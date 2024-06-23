package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.FunctionCall
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import ru.scisolutions.scicmscore.engine.model.AggregateType
import ru.scisolutions.scicmscore.engine.model.Column
import ru.scisolutions.scicmscore.engine.model.input.DatasetFieldInput
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset

class DatasetSqlExprEvaluator {
    fun evaluate(dataset: Dataset, table: DbTable, fieldName: String, omitAlias: Boolean = false): CustomSql {
        val input = toFieldInput(dataset, fieldName)

        return evaluate(dataset, table, input, omitAlias)
    }

    private fun toFieldInput(dataset: Dataset, fieldName: String): DatasetFieldInput {
        val field = dataset.spec.getField(fieldName)

        return DatasetFieldInput(
            name = fieldName,
            custom = field.custom,
            source = field.source,
            aggregate = field.aggregate,
            formula = field.formula
        )
    }

    fun evaluate(dataset: Dataset, table: DbTable, input: DatasetFieldInput, omitAlias: Boolean = false): CustomSql {
        if (input.aggregate == null) {
            if (input.formula == null) {
                return CustomSql(if (input.source == null || omitAlias) "${table.alias}.${input.name}" else "${table.alias}.${input.source} AS ${input.name}")
            } else {
                val expr = input.formula.replace(fieldRegex) {
                    val colName = it.value.trim('[', ']')

                    // Validate formula column
                    if (colName !in dataset.spec.columns)
                        throw IllegalArgumentException("Illegal formula [${input.formula}].")

                    val column = dataset.spec.getField(colName)
                    if (column.custom)
                        throw IllegalArgumentException("Custom columns ($colName) cannot be used in formulas.")

                    "${table.alias}.$colName"
                }

                return CustomSql(if (omitAlias) "($expr)" else "($expr) AS ${input.name}")
            }
        }

        val column = DbColumn(table, input.source ?: input.name, null, null)
        val aggregateFunctionCall = buildAggregateFunctionCall(column, input.aggregate)
        return CustomSql(if (omitAlias) aggregateFunctionCall else "$aggregateFunctionCall AS ${input.name}")
    }

    fun buildAggregateFunctionCall(aggregateCol: DbColumn, aggregateType: AggregateType): FunctionCall =
        when (aggregateType) {
            AggregateType.count -> FunctionCall.count().addColumnParams(aggregateCol)
            AggregateType.countd -> FunctionCall.count().addCustomParams(CustomSql("DISTINCT ${aggregateCol.table.alias}.${aggregateCol.name}"))
            AggregateType.sum -> FunctionCall.sum().addColumnParams(aggregateCol)
            AggregateType.avg -> FunctionCall.avg().addColumnParams(aggregateCol)
            AggregateType.min -> FunctionCall.min().addColumnParams(aggregateCol)
            AggregateType.max -> FunctionCall.max().addColumnParams(aggregateCol)
        }

    fun isAggregate(dataset: Dataset, fieldName: String): Boolean =
        isAggregate(toFieldInput(dataset, fieldName))

    fun isAggregate(input: DatasetFieldInput): Boolean =
        input.custom && ((input.source != null && input.aggregate != null) || (input.formula != null && input.formula.contains(aggregateRegex)))

    fun calculateType(columns: Map<String, Column>, colName: String, column: Column): FieldType =
        calculateType(
            columns,
            DatasetFieldInput(
                name = colName,
                custom = column.custom,
                source = column.source,
                aggregate = column.aggregate,
                formula = column.formula
            )
        )

    fun calculateType(columns: Map<String, Column>, input: DatasetFieldInput): FieldType {
        if (input.aggregate == null) {
            return if (input.formula == null)
                requireNotNull(columns[input.source ?: input.name]).typeRequired
            else FieldType.decimal
        }

        val source = input.source ?: throw IllegalArgumentException("Source column must be set for aggregation.")
        val field = columns[source] ?: throw IllegalArgumentException("Source column not found.")

        return calculateAggregationResultType(field.typeRequired, input.aggregate)
    }

    fun calculateAggregationResultType(type: FieldType, aggregationType: AggregateType): FieldType =
        when (aggregationType) {
            AggregateType.count, AggregateType.countd -> FieldType.int
            AggregateType.sum, AggregateType.avg, AggregateType.min, AggregateType.max -> type
        }

    companion object {
        private val fieldRegex = "\\[\\w+]".toRegex()
        private val aggregateRegex = "(COUNT|COUNTD|SUM|AVG|MIN|MAX)\\s*\\(".toRegex(RegexOption.IGNORE_CASE)
    }
}