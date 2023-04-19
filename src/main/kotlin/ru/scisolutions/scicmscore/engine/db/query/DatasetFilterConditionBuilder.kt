package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.BetweenCondition
import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.NotCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.input.DatasetFiltersInput
import ru.scisolutions.scicmscore.engine.model.input.PrimitiveFilterInput
import kotlin.random.Random

@Component
class DatasetFilterConditionBuilder {
    fun newFilterCondition(
        datasetFiltersInput: DatasetFiltersInput,
        schema: DbSchema,
        table: DbTable,
        query: SelectQuery,
        paramSource: DatasetSqlParameterSource
    ): Condition {
        val nestedConditions = mutableListOf<Condition>()

        datasetFiltersInput.fieldFilters.forEach { (fieldName, fieldFilter) ->
            val column = DbColumn(table, fieldName, null, null)
            nestedConditions.add(newPrimitiveCondition(fieldFilter, table, column, paramSource))
        }

        datasetFiltersInput.andFilterList?.let { list ->
            val andConditions = list.map { newFilterCondition(it, schema, table, query, paramSource) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        datasetFiltersInput.orFilterList?.let { list ->
            val orConditions = list.map { newFilterCondition(it, schema, table, query, paramSource) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.OR, *orConditions.toTypedArray()))
        }

        datasetFiltersInput.notFilter?.let {
            nestedConditions.add(NotCondition(newFilterCondition(it, schema, table, query, paramSource)))
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }

    private fun newPrimitiveCondition(primitiveFilterInput: PrimitiveFilterInput, table: DbTable, column: DbColumn, paramSource: DatasetSqlParameterSource): Condition {
        val nestedConditions = mutableListOf<Condition>()
        val sqlParamName = "${table.alias}_${column.name}_${Random.nextInt(0, 1000)}" // TODO: Change to truly unique name

        primitiveFilterInput.containsFilter?.let {
            nestedConditions.add(BinaryCondition.like(column, "%$it%"))
        }

        primitiveFilterInput.containsiFilter?.let {
            nestedConditions.add(BinaryCondition.like(CustomSql("LOWER(${table.alias}.${column.name})"), "%${it.lowercase()}%"))
        }

        primitiveFilterInput.notContainsFilter?.let {
            nestedConditions.add(BinaryCondition.notLike(column, "%$it%"))
        }

        primitiveFilterInput.notContainsiFilter?.let {
            nestedConditions.add(BinaryCondition.notLike(CustomSql("LOWER(${table.alias}.${column.name})"), "%${it.lowercase()}%"))
        }

        primitiveFilterInput.startsWithFilter?.let {
            nestedConditions.add(BinaryCondition.like(column, "$it%"))
        }

        primitiveFilterInput.endsWithFilter?.let {
            nestedConditions.add(BinaryCondition.like(column, "%$it"))
        }

        primitiveFilterInput.eqFilter?.let {
            val eqSqlParamName = "${sqlParamName}_eq"
            nestedConditions.add(BinaryCondition.equalTo(column, CustomSql(":$eqSqlParamName")))
            paramSource.addValueWithParsing(eqSqlParamName, it)
        }

        primitiveFilterInput.neFilter?.let {
            val neSqlParamName = "${sqlParamName}_ne"
            nestedConditions.add(BinaryCondition.notEqualTo(column, CustomSql(":$neSqlParamName")))
            paramSource.addValueWithParsing(neSqlParamName, it)
        }

        primitiveFilterInput.gtFilter?.let {
            val gtSqlParamName = "${sqlParamName}_gt"
            nestedConditions.add(BinaryCondition.greaterThan(column, CustomSql(":$gtSqlParamName")))
            paramSource.addValueWithParsing(gtSqlParamName, it)
        }

        primitiveFilterInput.gteFilter?.let {
            val gteSqlParamName = "${sqlParamName}_gte"
            nestedConditions.add(BinaryCondition.greaterThanOrEq(column, CustomSql(":$gteSqlParamName")))
            paramSource.addValueWithParsing(gteSqlParamName, it)
        }

        primitiveFilterInput.ltFilter?.let {
            val ltSqlParamName = "${sqlParamName}_lt"
            nestedConditions.add(BinaryCondition.lessThan(column, CustomSql(":$ltSqlParamName")))
            paramSource.addValueWithParsing(ltSqlParamName, it)
        }

        primitiveFilterInput.lteFilter?.let {
            val lteSqlParamName = "${sqlParamName}_lte"
            nestedConditions.add(BinaryCondition.lessThanOrEq(column, CustomSql(":$lteSqlParamName")))
            paramSource.addValueWithParsing(lteSqlParamName, it)
        }

        primitiveFilterInput.betweenFilter?.let {
            val leftSqlParamName = "${sqlParamName}_left"
            val rightSqlParamName = "${sqlParamName}_right"
            nestedConditions.add(BetweenCondition(column, CustomSql(":$leftSqlParamName"), CustomSql(":$rightSqlParamName")))
            paramSource
                .addValueWithParsing(leftSqlParamName, it.left)
                .addValueWithParsing(rightSqlParamName, it.right)
        }

        primitiveFilterInput.inFilter?.let { list ->
            val arr = list.map { SQL.toSqlValueWithParsing(it) }.toTypedArray()
            nestedConditions.add(InCondition(column, *arr))
        }

        primitiveFilterInput.notInFilter?.let { list ->
            val arr = list.map { SQL.toSqlValueWithParsing(it) }.toTypedArray()
            nestedConditions.add(NotCondition(InCondition(column, *arr)))
        }

        if (primitiveFilterInput.nullFilter == true || primitiveFilterInput.nullFilter == "true") {
            nestedConditions.add(UnaryCondition.isNull(column))
        }

        if (primitiveFilterInput.notNullFilter == true || primitiveFilterInput.notNullFilter == "true") {
            nestedConditions.add(UnaryCondition.isNotNull(column))
        }

        primitiveFilterInput.andFilterList?.let { list ->
            val andConditions = list.map { newPrimitiveCondition(it, table, column, paramSource) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        primitiveFilterInput.orFilterList?.let { list ->
            val orConditions = list.map { newPrimitiveCondition(it, table, column, paramSource) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.OR, *orConditions.toTypedArray()))
        }

        primitiveFilterInput.notFilter?.let {
            nestedConditions.add(NotCondition(newPrimitiveCondition(it, table, column, paramSource)))
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }
}