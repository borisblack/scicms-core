package ru.scisolutions.scicmscore.engine.db.query

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.FunctionCall
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.db.paginator.DatasetPaginator
import ru.scisolutions.scicmscore.engine.model.AggregateType
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
        val query = buildInitialLoadQuery(dataset, input, schema, table, paramSource)

        if (input.aggregate != null && !input.aggregateField.isNullOrBlank()) {
            val wrapTable = DbTable(schema, "($query)")
            val aggregateCol = DbColumn(wrapTable, input.aggregateField, null, null)
            val aggregateQuery = SelectQuery()
                .addCustomColumns(
                    CustomSql("${buildAggregateFunctionCall(aggregateCol, input.aggregate)} AS ${input.aggregateField}")
                )
                .addFromTable(wrapTable)

            if (!input.groupFields.isNullOrEmpty()) {
                for (groupField in input.groupFields) {
                    val groupCol = DbColumn(wrapTable, groupField, null, null)
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
        if (!input.sort.isNullOrEmpty())
            datasetOrderingsParser.parseOrderings(input.sort, table, query)

        val pagination: Pagination? =
            if (input.pagination == null) null else datasetPaginator.paginate(dataset, input.pagination, query, paramSource)

        return DatasetQuery(
            sql = query.validate().toString(),
            pagination = pagination
        )
    }

    private fun validateDatasetInput(dataset: Dataset, input: DatasetInput) {
        if (input.aggregate == null || input.aggregateField.isNullOrBlank())
            return

        if (hasAggregation(dataset, input))
            throw IllegalArgumentException("Duplicate aggregation clause.")
    }

    private fun hasAggregation(dataset: Dataset, input: DatasetInput): Boolean =
        if (input.fields.isNullOrEmpty())
            hasAggregation(dataset)
        else input.fields.any { it.aggregate != null }

    private fun hasAggregation(dataset: Dataset): Boolean =
        dataset.spec.columns.values.any { it.aggregate != null && it.isVisible }

    private fun hasAggregation(input: DatasetInput): Boolean =
        input.fields?.any { it.aggregate != null } == true

    private fun buildInitialLoadQuery(
        dataset: Dataset,
        input: DatasetInput,
        schema: DbSchema,
        table: DbTable,
        paramSource: DatasetSqlParameterSource
    ): SelectQuery {
        val query = SelectQuery()

        // Columns
        val customColumns: Array<CustomSql> =
            if (input.fields.isNullOrEmpty()) {
                dataset.spec.columns
                    .filterValues { it.isVisible }
                    .map { (colName, col) ->
                        val column = DbColumn(table, col.source ?: colName, null, null)
                        buildCustomColumn(column, colName, col.aggregate)
                    }
                    .toTypedArray()
            } else {
                input.fields
                    .map {
                        val column = DbColumn(table, it.source ?: it.name, null, null)
                        buildCustomColumn(column, it.name, it.aggregate)
                    }
                    .toTypedArray()
            }
        query.addCustomColumns(*customColumns)
            .addFromTable(table)

        // Groupings
        if (hasAggregation(dataset, input)) {
            val groupingColumns: Array<DbColumn> =
                if (input.fields.isNullOrEmpty()) {
                    dataset.spec.columns
                        .filterValues { it.aggregate == null && it.isVisible }
                        .map { (colName, _) -> DbColumn(table, colName, null, null) }
                        .toTypedArray()
                } else {
                    input.fields
                        .filter { it.aggregate == null }
                        .map { DbColumn(table, it.name, null, null) }
                        .toTypedArray()
                }
            query.addGroupings(*groupingColumns)
        }

        // Filters
        if (input.filters != null) {
            query.addCondition(
                datasetFilterConditionBuilder.newFilterCondition(
                    dataset = dataset,
                    datasetFiltersInput = input.filters,
                    schema = schema,
                    table = table,
                    query = query,
                    paramSource = paramSource
                )
            )
        }

        return query.validate()
    }

    private fun buildCustomColumn(column: DbColumn, alias: String, aggregate: AggregateType?): CustomSql {
        if (aggregate == null)
            return CustomSql(if (alias == column.name) "${column.table.alias}.${column.name}" else "${column.table.alias}.${column.name} AS $alias")

        return CustomSql("${buildAggregateFunctionCall(column, aggregate)} AS ${if (alias == column.name) column.name else alias}")
    }

    private fun buildAggregateFunctionCall(aggregateCol: DbColumn, aggregateType: AggregateType): FunctionCall =
        when (aggregateType) {
            AggregateType.count -> FunctionCall.count().addColumnParams(aggregateCol)
            AggregateType.countd -> FunctionCall.count().addCustomParams(CustomSql("DISTINCT ${aggregateCol.table.alias}.${aggregateCol.name}"))
            AggregateType.sum -> FunctionCall.sum().addColumnParams(aggregateCol)
            AggregateType.avg -> FunctionCall.avg().addColumnParams(aggregateCol)
            AggregateType.min -> FunctionCall.min().addColumnParams(aggregateCol)
            AggregateType.max -> FunctionCall.max().addColumnParams(aggregateCol)
        }

    companion object {
        private val datasetOrderingsParser = DatasetOrderingsParser()
    }
}