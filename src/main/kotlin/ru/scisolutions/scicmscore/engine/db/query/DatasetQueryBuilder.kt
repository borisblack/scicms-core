package ru.scisolutions.scicmscore.engine.db.query

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.FunctionCall
import com.healthmarketscience.sqlbuilder.OrderObject.Dir
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.db.paginator.DatasetPaginator
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.model.AggregateType
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.persistence.entity.Dataset.TemporalType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime

@Component
class DatasetQueryBuilder(
    private val datasetFilterConditionBuilder: DatasetFilterConditionBuilder,
    private val datasetPaginator: DatasetPaginator
) {
    @JsonInclude(Include.NON_NULL)
    class DatasetQuery(
        val sql: String,
        val pagination: Pagination
    )

    fun buildLoadQuery(
        dataset: Dataset,
        start: String?,
        end: String?,
        aggregateType: AggregateType?,
        groupBy: String?,
        paramSource: DatasetSqlParameterSource
    ): SelectQuery {
        val spec = DbSpec()
        val schema = spec.addDefaultSchema()
        val table = DbTable(schema, dataset.getQueryOrThrow())
        val query = SelectQuery()
            .addAllColumns()
            .addFromTable(table)

        if (dataset.temporalField != null) {
            val temporalCol = DbColumn(table, dataset.temporalField, null, null)
            if (start != null) {
                val startTemporal = parseTemporal(start, dataset.temporalType)
                val sqlParamName = "${table.alias}_start"
                query.addCondition(BinaryCondition.greaterThanOrEq(temporalCol, CustomSql(":$sqlParamName")))
                paramSource.addValue(sqlParamName, startTemporal, dataset.temporalType)
            }

            if (end != null) {
                val endTemporal = parseTemporal(end, dataset.temporalType)
                val sqlParamName = "${table.alias}_end"
                query.addCondition(BinaryCondition.lessThanOrEq(temporalCol, CustomSql(":$sqlParamName")))
                paramSource.addValue(sqlParamName, endTemporal, dataset.temporalType)
            }

            query.addOrdering(temporalCol, Dir.ASCENDING)
        }

        if (aggregateType != null) {
            val wrapTable = DbTable(schema, "(${query.validate()})")
            val metricCol = DbColumn(wrapTable, dataset.metricField, null, null)
            val wrapQuery = SelectQuery()
                .addCustomColumns(
                    CustomSql("${getFunctionCall(metricCol, aggregateType)} AS ${dataset.metricField}")
                )
                .addFromTable(wrapTable)

            if (aggregateType != AggregateType.countAll) {
                if (groupBy == null)
                    throw IllegalArgumentException("The groupBy parameter must be specified")

                val groupByCol = DbColumn(wrapTable, groupBy, null, null)
                wrapQuery
                    .addColumns(groupByCol)
                    .addGroupings(groupByCol)
            }

            return wrapQuery.validate()
        }

        return query.validate()
    }

    private fun parseTemporal(temporal: String, temporalType: TemporalType): Any =
        when (temporalType) {
            TemporalType.date -> LocalDate.parse(temporal)
            TemporalType.time -> OffsetTime.parse(temporal)
            TemporalType.datetime, TemporalType.timestamp -> OffsetDateTime.parse(temporal)
        }

    private fun getFunctionCall(metricCol: DbColumn, aggregateType: AggregateType): FunctionCall =
        when (aggregateType) {
            AggregateType.countAll -> FunctionCall.countAll()
            AggregateType.count -> FunctionCall.count().addColumnParams(metricCol)
            AggregateType.sum -> FunctionCall.sum().addColumnParams(metricCol)
            AggregateType.avg -> FunctionCall.avg().addColumnParams(metricCol)
            AggregateType.min -> FunctionCall.min().addColumnParams(metricCol)
            AggregateType.max -> FunctionCall.max().addColumnParams(metricCol)
        }

    fun buildLoadQuery(dataset: Dataset, input: DatasetInput, paramSource: DatasetSqlParameterSource): DatasetQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val query = buildInitialLoadQuery(dataset, input, schema, paramSource)
        val table = schema.findTable(dataset.getQueryOrThrow()) ?: throw IllegalArgumentException("Query for dataset not found in schema")

        val pagination = datasetPaginator.paginate(dataset, input.pagination, query, paramSource)

        // Sort
        if (!input.sort.isNullOrEmpty()) {
            orderingsParser.parseOrderings(item, input.sort, schema, query, table)
        }

        return DatasetQuery(
            sql = query.validate().toString(),
            pagination = pagination
        )
    }

    private fun buildInitialLoadQuery(dataset: Dataset, input: DatasetInput, schema: DbSchema, paramSource: DatasetSqlParameterSource): SelectQuery {
        val table = schema.addTable(dataset.getQueryOrThrow())
        val query = SelectQuery()

        if (input.fields == null) {
            query.addAllColumns()
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
                    datasetFilterInput = input.filters,
                    schema = schema,
                    table = table,
                    query = query,
                    paramSource = paramSource
                )
            )
        }

        return query.validate()
    }
}