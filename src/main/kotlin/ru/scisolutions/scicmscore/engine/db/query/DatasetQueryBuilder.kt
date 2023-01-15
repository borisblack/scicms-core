package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.FunctionCall
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import ru.scisolutions.scicmscore.model.AggregateType
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.persistence.entity.Dataset.TemporalType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime

class DatasetQueryBuilder {
    fun buildFindAllQuery(
        dataset: Dataset,
        start: String?,
        end: String?,
        aggregateType: AggregateType?,
        paramSource: DatasetSqlParameterSource
    ): SelectQuery {
        val spec = DbSpec()
        val schema = spec.addDefaultSchema()
        val table = DbTable(schema, dataset.getQueryOrThrow())
        val query = SelectQuery().addAllColumns()

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

        if (aggregateType != null) {
            val wrapTable = DbTable(schema, "(${query.validate()})")
            val labelCol = DbColumn(wrapTable, dataset.labelField, null, null)
            val metricCol = DbColumn(wrapTable, dataset.metricField, null, null)
            val wrapQuery = SelectQuery()
                .addCustomColumns(
                    labelCol,
                    getFunctionCall(metricCol, aggregateType)
                )

            if (aggregateType != AggregateType.countAll)
                wrapQuery.addGroupings(labelCol)

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
}