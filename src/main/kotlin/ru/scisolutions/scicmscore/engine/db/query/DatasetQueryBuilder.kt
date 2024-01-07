package ru.scisolutions.scicmscore.engine.db.query

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.db.paginator.DatasetPaginator
import ru.scisolutions.scicmscore.engine.model.input.DatasetFieldInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetFiltersInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.persistence.entity.Dataset

@Component
class DatasetQueryBuilder(
    private val datasetFilterConditionBuilder: DatasetFilterConditionBuilder,
    private val datasetPaginator: DatasetPaginator
) {
    @JsonInclude(Include.NON_NULL)
    class DatasetQuery(
        val sql: String,
        val pagination: Pagination?
    )

    fun buildLoadQuery(dataset: Dataset, input: DatasetInput, paramSource: DatasetSqlParameterSource): DatasetQuery {
        validateDatasetInput(dataset, input)

        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = schema.addTable(dataset.qs)
        val query = buildInitialLoadQuery(dataset, input, table, paramSource)

        // TODO: Deprecated feature, will be removed
        if (input.aggregate != null && !input.aggregateField.isNullOrBlank()) {
            val wrapTable = DbTable(schema, "($query)")
            val datasetAggregateColumn = dataset.spec.getField(input.aggregateField)
            val aggregateCol = DbColumn(wrapTable, datasetAggregateColumn.source ?: input.aggregateField, null, null)
            val aggregateQuery = SelectQuery()
                .addCustomColumns(
                    CustomSql("${datasetSqlExprEvaluator.buildAggregateFunctionCall(aggregateCol, input.aggregate)} AS ${input.aggregateField}")
                )
                .addFromTable(wrapTable)

            if (!input.groupFields.isNullOrEmpty()) {
                for (groupField in input.groupFields) {
                    val datasetGroupColumn = dataset.spec.getField(groupField)
                    val groupCol = DbColumn(wrapTable, datasetGroupColumn.source ?: groupField, null, null)
                    aggregateQuery
                        .addColumns(groupCol)
                        .addGroupings(groupCol)
                }
            }

            // Sort
            if (!input.sort.isNullOrEmpty())
                datasetOrderingsParser.parseOrderings(input.sort, null, aggregateQuery)

            val pagination: Pagination? =
                if (input.pagination == null) null else datasetPaginator.paginate(dataset, input.pagination, aggregateQuery, paramSource)

            return DatasetQuery(
                sql = aggregateQuery.validate().toString(),
                pagination = pagination
            )
        }

        // Sort
        if (!input.sort.isNullOrEmpty()) {
            datasetOrderingsParser.parseOrderings(
                input.sort,
                null, // no table for custom fields
                query)
        }

        val pagination: Pagination? =
            if (input.pagination == null) null else datasetPaginator.paginate(dataset, input.pagination, query, paramSource)

        return DatasetQuery(
            sql = query.validate().toString(),
            pagination = pagination
        )
    }

    private fun validateDatasetInput(dataset: Dataset, input: DatasetInput) {
        dataset.spec.validate()

        if (input.fields != null && !validateFields(input.fields))
            throw IllegalArgumentException("Illegal fields input.")

        if (input.aggregate == null || input.aggregateField.isNullOrBlank())
            return

        if (hasAggregation(dataset, input))
            throw IllegalArgumentException("Duplicate aggregation clause.")
    }

    private fun validateFields(fields: List<DatasetFieldInput>) =
        fields.all { (it.source == null && it.formula == null && it.aggregate == null) || it.custom }

    private fun hasAggregation(dataset: Dataset, input: DatasetInput): Boolean =
        if (input.fields.isNullOrEmpty()) hasAggregation(dataset) else hasAggregation(input)

    private fun hasAggregation(input: DatasetInput): Boolean =
        input.fields?.any { datasetSqlExprEvaluator.isAggregate(it) } == true

    fun hasAggregation(dataset: Dataset): Boolean =
        dataset.spec.columns.any { (fieldName, field) ->
            !field.hidden && datasetSqlExprEvaluator.isAggregate(dataset, fieldName)
        }

    fun whereFiltersInput(dataset: Dataset, filters: DatasetFiltersInput): DatasetFiltersInput =
        DatasetFiltersInput(
            fieldFilters = filters.fieldFilters.filterKeys { !datasetSqlExprEvaluator.isAggregate(dataset, it) },
            andFiltersList = filters.andFilterList?.map { whereFiltersInput(dataset, it) },
            orFiltersList = filters.orFilterList?.map { whereFiltersInput(dataset, it) },
            notFilters = filters.notFilter?.let { whereFiltersInput(dataset, it) }
        )

    fun havingFiltersInput(dataset: Dataset, filters: DatasetFiltersInput): DatasetFiltersInput =
        DatasetFiltersInput(
            fieldFilters = filters.fieldFilters.filterKeys { datasetSqlExprEvaluator.isAggregate(dataset, it) },
            andFiltersList = filters.andFilterList?.map { havingFiltersInput(dataset, it) },
            orFiltersList = filters.orFilterList?.map { havingFiltersInput(dataset, it) },
            notFilters = filters.notFilter?.let { havingFiltersInput(dataset, it) }
        )

    private fun buildInitialLoadQuery(
        dataset: Dataset,
        input: DatasetInput,
        table: DbTable,
        paramSource: DatasetSqlParameterSource
    ): SelectQuery {
        val query = SelectQuery()

        // Select columns
        val customColumns: Array<CustomSql> =
            if (input.fields.isNullOrEmpty()) {
                dataset.spec.columns
                    .filterValues { !it.hidden }
                    .map { (colName, col) ->
                        val fieldInput = DatasetFieldInput(
                            name = colName,
                            custom = col.custom,
                            source = col.source,
                            formula = col.formula,
                            aggregate = col.aggregate
                        )
                        datasetSqlExprEvaluator.evaluate(dataset, table, fieldInput)
                    }
                    .toTypedArray()
            } else {
                input.fields
                    .map {
                        datasetSqlExprEvaluator.evaluate(dataset, table, it)
                    }
                    .toTypedArray()
            }
        query.addCustomColumns(*customColumns)
            .addFromTable(table)

        // Filters
        if (input.filters != null) {
            val whereFilters = whereFiltersInput(dataset, input.filters)
            if (whereFilters.isNotEmpty()) {
                query.addCondition(
                    datasetFilterConditionBuilder.newFilterCondition(
                        dataset = dataset,
                        datasetFiltersInput = whereFilters,
                        table = table,
                        query = query,
                        paramSource = paramSource
                    )
                )
            }

            val havingFilters = havingFiltersInput(dataset, input.filters)
            if (havingFilters.isNotEmpty()) {
                query.addHaving(
                    datasetFilterConditionBuilder.newFilterCondition(
                        dataset = dataset,
                        datasetFiltersInput = havingFilters,
                        table = table,
                        query = query,
                        paramSource = paramSource
                    )
                )
            }
        }

        // Groupings
        if (hasAggregation(dataset, input)) {
            val groupingColumns: Array<DbColumn> =
                if (input.fields.isNullOrEmpty()) {
                    dataset.spec.columns
                        .filter { (fieldName, field) -> !field.hidden && !datasetSqlExprEvaluator.isAggregate(dataset, fieldName) }
                        .map { (colName, col) -> DbColumn(table, col.source ?: colName, null, null) }
                        .toTypedArray()
                } else {
                    input.fields
                        .filter { !datasetSqlExprEvaluator.isAggregate(it) }
                        .map { DbColumn(table, it.source ?: it.name, null, null) }
                        .toTypedArray()
                }
            query.addGroupings(*groupingColumns)
        }

        return query.validate()
    }

    companion object {
        private val datasetSqlExprEvaluator = DatasetSqlExprEvaluator()
        private val datasetOrderingsParser = DatasetOrderingsParser()
    }
}