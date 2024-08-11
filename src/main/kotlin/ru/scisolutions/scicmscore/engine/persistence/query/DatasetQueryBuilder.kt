package ru.scisolutions.scicmscore.engine.persistence.query

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.input.DatasetFieldInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetFiltersInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset
import ru.scisolutions.scicmscore.engine.persistence.paginator.DatasetPaginator

@Component
class DatasetQueryBuilder(
    private val datasetFilterConditionBuilder: DatasetFilterConditionBuilder,
    private val datasetPaginator: DatasetPaginator,
) {
    @JsonInclude(Include.NON_NULL)
    class DatasetQuery(
        val sql: String,
        val pagination: Pagination?,
    )

    fun buildLoadQuery(dataset: Dataset, input: DatasetInput, paramSource: DatasetSqlParameterSource): DatasetQuery {
        validateDatasetInput(dataset, input)

        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = schema.addTable(dataset.qs)
        val query = buildInitialLoadQuery(dataset, input, table, paramSource)

        // Sort
        if (!input.sort.isNullOrEmpty()) {
            datasetOrderingsParser.parseOrderings(
                input.sort,
                null, // no table for custom fields
                query,
            )
        }

        val pagination: Pagination? =
            if (input.pagination == null) null else datasetPaginator.paginate(dataset, input.pagination, query, paramSource)

        return DatasetQuery(
            sql = query.validate().toString(),
            pagination = pagination,
        )
    }

    private fun validateDatasetInput(dataset: Dataset, input: DatasetInput) {
        dataset.spec.validate()

        if (input.fields != null && !validateFields(input.fields)) {
            throw IllegalArgumentException("Illegal fields input.")
        }
    }

    private fun validateFields(fields: List<DatasetFieldInput>) = fields.all { (it.source == null && it.formula == null && it.aggregate == null) || it.custom }

    private fun hasAggregation(dataset: Dataset, input: DatasetInput): Boolean = if (input.fields.isNullOrEmpty()) hasAggregation(dataset) else hasAggregation(input)

    private fun hasAggregation(input: DatasetInput): Boolean = input.fields?.any { datasetSqlExprEvaluator.isAggregate(it) } == true

    fun hasAggregation(dataset: Dataset): Boolean = dataset.spec.columns.any { (fieldName, field) ->
        !field.hidden && datasetSqlExprEvaluator.isAggregate(dataset, fieldName)
    }

    fun whereFiltersInput(dataset: Dataset, fields: Map<String, DatasetFieldInput>, filters: DatasetFiltersInput): DatasetFiltersInput = DatasetFiltersInput(
        fieldFilters =
        filters.fieldFilters.filterKeys {
            val fieldInput = fields[it]
            if (fieldInput == null) {
                !datasetSqlExprEvaluator.isAggregate(
                    dataset,
                    it,
                )
            } else {
                !datasetSqlExprEvaluator.isAggregate(fieldInput)
            }
        },
        andFiltersList = filters.andFilterList?.map { whereFiltersInput(dataset, fields, it) },
        orFiltersList = filters.orFilterList?.map { whereFiltersInput(dataset, fields, it) },
        notFilters = filters.notFilter?.let { whereFiltersInput(dataset, fields, it) },
    )

    fun havingFiltersInput(dataset: Dataset, fields: Map<String, DatasetFieldInput>, filters: DatasetFiltersInput): DatasetFiltersInput = DatasetFiltersInput(
        fieldFilters =
        filters.fieldFilters.filterKeys {
            val fieldInput = fields[it]
            if (fieldInput == null) {
                datasetSqlExprEvaluator.isAggregate(
                    dataset,
                    it,
                )
            } else {
                datasetSqlExprEvaluator.isAggregate(fieldInput)
            }
        },
        andFiltersList = filters.andFilterList?.map { havingFiltersInput(dataset, fields, it) },
        orFiltersList = filters.orFilterList?.map { havingFiltersInput(dataset, fields, it) },
        notFilters = filters.notFilter?.let { havingFiltersInput(dataset, fields, it) },
    )

    private fun buildInitialLoadQuery(dataset: Dataset, input: DatasetInput, table: DbTable, paramSource: DatasetSqlParameterSource): SelectQuery {
        val query = SelectQuery()

        // Select columns
        val customColumns: Array<CustomSql> =
            if (input.fields.isNullOrEmpty()) {
                dataset.spec.columns
                    .filterValues { !it.hidden }
                    .map { (fieldName, _) ->
                        datasetSqlExprEvaluator.evaluate(dataset, table, fieldName)
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
        val fields = input.fields?.associateBy { it.name } ?: emptyMap()
        if (input.filters != null) {
            val whereFilters = whereFiltersInput(dataset, fields, input.filters)
            if (whereFilters.isNotEmpty()) {
                query.addCondition(
                    datasetFilterConditionBuilder.newFilterCondition(
                        dataset = dataset,
                        fields,
                        datasetFiltersInput = whereFilters,
                        table = table,
                        query = query,
                        paramSource = paramSource,
                    ),
                )
            }

            val havingFilters = havingFiltersInput(dataset, fields, input.filters)
            if (havingFilters.isNotEmpty()) {
                query.addHaving(
                    datasetFilterConditionBuilder.newFilterCondition(
                        dataset = dataset,
                        fields,
                        datasetFiltersInput = havingFilters,
                        table = table,
                        query = query,
                        paramSource = paramSource,
                    ),
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
