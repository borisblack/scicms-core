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
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.engine.model.AggregateType
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
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = schema.addTable(dataset.getQueryOrThrow())
        val query = buildInitialLoadQuery(input, schema, table, paramSource)

        if (input.aggregate != null && !input.aggregateField.isNullOrBlank()) {
            val wrapTable = DbTable(schema, "(${query.validate()})")
            val aggregateCol = DbColumn(wrapTable, input.aggregateField, null, null)
            val aggregateQuery = SelectQuery()
                .addCustomColumns(
                    CustomSql("${buildAggregateFunctionCall(aggregateCol, input.aggregate)} AS ${input.aggregateField}")
                )
                .addFromTable(wrapTable)

            if (input.aggregate != AggregateType.countAll && !input.groupField.isNullOrBlank()) {
                val groupCol = DbColumn(wrapTable, input.groupField, null, null)
                aggregateQuery
                    .addColumns(groupCol)
                    .addGroupings(groupCol)
            }

            // Sort
            if (!input.sort.isNullOrEmpty() && input.aggregate != AggregateType.countAll) {
                datasetOrderingsParser.parseOrderings(input.sort, schema, wrapTable, aggregateQuery)
            }

            val pagination: Pagination? =
                if (input.pagination == null || input.aggregate == AggregateType.countAll) null
                else datasetPaginator.paginate(dataset, input.pagination, aggregateQuery, paramSource)

            return DatasetQuery(
                sql = aggregateQuery.validate().toString(),
                pagination = pagination
            )
        }

        // Sort
        if (!input.sort.isNullOrEmpty()) {
            datasetOrderingsParser.parseOrderings(input.sort, schema, table, query)
        }

        val pagination: Pagination? =
            if (input.pagination == null) null else datasetPaginator.paginate(dataset, input.pagination, query, paramSource)

        return DatasetQuery(
            sql = query.validate().toString(),
            pagination = pagination
        )
    }

    private fun buildInitialLoadQuery(
        input: DatasetInput,
        schema: DbSchema,
        table: DbTable,
        paramSource: DatasetSqlParameterSource
    ): SelectQuery {
        val query = SelectQuery()

        if (input.fields == null) {
            query.addAllColumns()
                .addFromTable(table)
        } else {
            val columns = input.fields
                .map { DbColumn(table, it, null, null) }
                .toTypedArray()
            query.addColumns(*columns)
        }

        // Filters
        if (input.filters != null) {
            query.addCondition(
                datasetFilterConditionBuilder.newFilterCondition(
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

    private fun buildAggregateFunctionCall(aggregateCol: DbColumn, aggregateType: AggregateType): FunctionCall =
        when (aggregateType) {
            AggregateType.countAll -> FunctionCall.countAll()
            AggregateType.count -> FunctionCall.count().addColumnParams(aggregateCol)
            AggregateType.sum -> FunctionCall.sum().addColumnParams(aggregateCol)
            AggregateType.avg -> FunctionCall.avg().addColumnParams(aggregateCol)
            AggregateType.min -> FunctionCall.min().addColumnParams(aggregateCol)
            AggregateType.max -> FunctionCall.max().addColumnParams(aggregateCol)
        }

    companion object {
        private val datasetOrderingsParser = DatasetOrderingsParser()
    }
}