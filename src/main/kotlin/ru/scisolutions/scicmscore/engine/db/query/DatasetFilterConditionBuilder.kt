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
import ru.scisolutions.scicmscore.persistence.entity.Dataset

@Component
class DatasetFilterConditionBuilder {
    fun newFilterCondition(
        dataset: Dataset,
        datasetFiltersInput: DatasetFiltersInput,
        schema: DbSchema,
        table: DbTable,
        query: SelectQuery,
        paramSource: DatasetSqlParameterSource,
        fieldNumbers: MutableMap<String, Int> = mutableMapOf()
    ): Condition {
        val nestedConditions = mutableListOf<Condition>()

        datasetFiltersInput.fieldFilters.forEach { (fieldName, fieldFilter) ->
            val column = DbColumn(table, fieldName, null, null)
            nestedConditions.add(
                newPrimitiveCondition(
                    dataset = dataset,
                    fieldName = fieldName,
                    primitiveFilterInput = fieldFilter,
                    table = table,
                    column = column,
                    paramSource = paramSource,
                    fieldNumbers = fieldNumbers
                )
            )
        }

        datasetFiltersInput.andFilterList?.let { list ->
            val andConditions = list.map {
                newFilterCondition(
                    dataset = dataset,
                    datasetFiltersInput = it,
                    schema = schema,
                    table = table,
                    query = query,
                    paramSource = paramSource,
                    fieldNumbers = fieldNumbers
                )
            }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        datasetFiltersInput.orFilterList?.let { list ->
            val orConditions = list.map {
                newFilterCondition(
                    dataset = dataset,
                    datasetFiltersInput = it,
                    schema = schema,
                    table = table,
                    query = query,
                    paramSource = paramSource,
                    fieldNumbers = fieldNumbers
                )
            }
            nestedConditions.add(ComboCondition(ComboCondition.Op.OR, *orConditions.toTypedArray()))
        }

        datasetFiltersInput.notFilter?.let {
            nestedConditions.add(
                NotCondition(
                    newFilterCondition(
                        dataset = dataset,
                        datasetFiltersInput = it,
                        schema = schema,
                        table = table,
                        query = query,
                        paramSource = paramSource,
                        fieldNumbers = fieldNumbers
                    )
                )
            )
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }

    private fun newPrimitiveCondition(
        dataset: Dataset,
        fieldName: String,
        primitiveFilterInput: PrimitiveFilterInput,
        table: DbTable,
        column: DbColumn,
        paramSource: DatasetSqlParameterSource,
        fieldNumbers: MutableMap<String, Int>
    ): Condition {
        val fieldType = dataset.spec.columns[fieldName]?.type
            ?: throw IllegalArgumentException("Field [$fieldName] not found.")

        val nestedConditions = mutableListOf<Condition>()
        val absFieldName = "${table.alias}_${column.name}"
        val fieldNumber = fieldNumbers.getOrDefault(absFieldName, 0)
        val sqlParamName = "${absFieldName}_$fieldNumber"
        fieldNumbers[absFieldName] = fieldNumber + 1

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
            paramSource.addValue(eqSqlParamName, it, fieldType)
        }

        primitiveFilterInput.neFilter?.let {
            val neSqlParamName = "${sqlParamName}_ne"
            nestedConditions.add(BinaryCondition.notEqualTo(column, CustomSql(":$neSqlParamName")))
            paramSource.addValue(neSqlParamName, it, fieldType)
        }

        primitiveFilterInput.gtFilter?.let {
            val gtSqlParamName = "${sqlParamName}_gt"
            nestedConditions.add(BinaryCondition.greaterThan(column, CustomSql(":$gtSqlParamName")))
            paramSource.addValue(gtSqlParamName, it, fieldType)
        }

        primitiveFilterInput.gteFilter?.let {
            val gteSqlParamName = "${sqlParamName}_gte"
            nestedConditions.add(BinaryCondition.greaterThanOrEq(column, CustomSql(":$gteSqlParamName")))
            paramSource.addValue(gteSqlParamName, it, fieldType)
        }

        primitiveFilterInput.ltFilter?.let {
            val ltSqlParamName = "${sqlParamName}_lt"
            nestedConditions.add(BinaryCondition.lessThan(column, CustomSql(":$ltSqlParamName")))
            paramSource.addValue(ltSqlParamName, it, fieldType)
        }

        primitiveFilterInput.lteFilter?.let {
            val lteSqlParamName = "${sqlParamName}_lte"
            nestedConditions.add(BinaryCondition.lessThanOrEq(column, CustomSql(":$lteSqlParamName")))
            paramSource.addValue(lteSqlParamName, it, fieldType)
        }

        primitiveFilterInput.betweenFilter?.let {
            val leftSqlParamName = "${sqlParamName}_left"
            val rightSqlParamName = "${sqlParamName}_right"
            nestedConditions.add(BetweenCondition(column, CustomSql(":$leftSqlParamName"), CustomSql(":$rightSqlParamName")))
            paramSource
                .addValue(leftSqlParamName, it.left, fieldType)
                .addValue(rightSqlParamName, it.right, fieldType)
        }

        primitiveFilterInput.inFilter?.let { list ->
            val arr = list.map { SQL.toSqlValue(it) }.toTypedArray()
            nestedConditions.add(InCondition(column, *arr))
        }

        primitiveFilterInput.notInFilter?.let { list ->
            val arr = list.map { SQL.toSqlValue(it) }.toTypedArray()
            nestedConditions.add(NotCondition(InCondition(column, *arr)))
        }

        if (primitiveFilterInput.nullFilter == true) {
            nestedConditions.add(UnaryCondition.isNull(column))
        }

        if (primitiveFilterInput.notNullFilter == true) {
            nestedConditions.add(UnaryCondition.isNotNull(column))
        }

        primitiveFilterInput.andFilterList?.let { list ->
            val andConditions = list.map {
                newPrimitiveCondition(
                    dataset = dataset,
                    fieldName = fieldName,
                    primitiveFilterInput = it,
                    table = table,
                    column = column,
                    paramSource = paramSource,
                    fieldNumbers = fieldNumbers
                )
            }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        primitiveFilterInput.orFilterList?.let { list ->
            val orConditions = list.map {
                newPrimitiveCondition(
                    dataset = dataset,
                    fieldName = fieldName,
                    primitiveFilterInput = it,
                    table = table,
                    column = column,
                    paramSource = paramSource,
                    fieldNumbers = fieldNumbers
                )
            }
            nestedConditions.add(ComboCondition(ComboCondition.Op.OR, *orConditions.toTypedArray()))
        }

        primitiveFilterInput.notFilter?.let {
            nestedConditions.add(
                NotCondition(
                    newPrimitiveCondition(
                        dataset = dataset,
                        fieldName = fieldName,
                        primitiveFilterInput = it,
                        table = table,
                        column = column,
                        paramSource = paramSource,
                        fieldNumbers = fieldNumbers
                    )
                )
            )
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }
}