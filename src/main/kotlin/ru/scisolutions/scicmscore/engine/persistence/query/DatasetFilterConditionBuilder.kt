package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.BetweenCondition
import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.NotCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.input.DatasetFieldInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetFiltersInput
import ru.scisolutions.scicmscore.engine.model.input.PrimitiveFilterInput
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset

@Component
class DatasetFilterConditionBuilder {
    fun newFilterCondition(
        dataset: Dataset,
        fields: Map<String, DatasetFieldInput>,
        datasetFiltersInput: DatasetFiltersInput,
        table: DbTable,
        query: SelectQuery,
        paramSource: DatasetSqlParameterSource,
        fieldNumbers: MutableMap<String, Int> = mutableMapOf()
    ): Condition {
        val nestedConditions = mutableListOf<Condition>()

        datasetFiltersInput.fieldFilters.forEach { (fieldName, fieldFilter) ->
            val field = fields[fieldName]
            val customSql =
                if (field == null) {
                    datasetSqlExprEvaluator.evaluate(dataset, table, fieldName, true)
                } else {
                    datasetSqlExprEvaluator.evaluate(dataset, table, field, true)
                }
            nestedConditions.add(
                newPrimitiveCondition(
                    dataset = dataset,
                    fields = fields,
                    fieldName = fieldName,
                    primitiveFilterInput = fieldFilter,
                    customSql = customSql,
                    paramSource = paramSource,
                    fieldNumbers = fieldNumbers
                )
            )
        }

        datasetFiltersInput.andFilterList?.let { list ->
            val andConditions =
                list.map {
                    newFilterCondition(
                        dataset = dataset,
                        fields = fields,
                        datasetFiltersInput = it,
                        table = table,
                        query = query,
                        paramSource = paramSource,
                        fieldNumbers = fieldNumbers
                    )
                }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        datasetFiltersInput.orFilterList?.let { list ->
            val orConditions =
                list.map {
                    newFilterCondition(
                        dataset = dataset,
                        fields = fields,
                        datasetFiltersInput = it,
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
                        fields = fields,
                        datasetFiltersInput = it,
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
        fields: Map<String, DatasetFieldInput>,
        fieldName: String,
        primitiveFilterInput: PrimitiveFilterInput,
        customSql: CustomSql,
        paramSource: DatasetSqlParameterSource,
        fieldNumbers: MutableMap<String, Int>
    ): Condition {
        val field = fields[fieldName]
        val fieldType =
            if (field == null) {
                dataset.spec.getField(fieldName).typeRequired
            } else {
                datasetSqlExprEvaluator.calculateType(dataset.spec.columns, field)
            }

        val nestedConditions = mutableListOf<Condition>()
        val absFieldName = customSql.toString().replace(nonWordRegex, "").lowercase()
        val fieldNumber = fieldNumbers.getOrDefault(absFieldName, 0)
        val sqlParamName = "${absFieldName}_$fieldNumber"
        fieldNumbers[absFieldName] = fieldNumber + 1

        primitiveFilterInput.containsFilter?.let {
            nestedConditions.add(BinaryCondition.like(customSql, "%$it%"))
        }

        primitiveFilterInput.containsiFilter?.let {
            nestedConditions.add(BinaryCondition.like(CustomSql("LOWER($customSql)"), "%${it.lowercase()}%"))
        }

        primitiveFilterInput.notContainsFilter?.let {
            nestedConditions.add(BinaryCondition.notLike(customSql, "%$it%"))
        }

        primitiveFilterInput.notContainsiFilter?.let {
            nestedConditions.add(BinaryCondition.notLike(CustomSql("LOWER($customSql)"), "%${it.lowercase()}%"))
        }

        primitiveFilterInput.startsWithFilter?.let {
            nestedConditions.add(BinaryCondition.like(customSql, "$it%"))
        }

        primitiveFilterInput.endsWithFilter?.let {
            nestedConditions.add(BinaryCondition.like(customSql, "%$it"))
        }

        primitiveFilterInput.eqFilter?.let {
            val eqSqlParamName = "${sqlParamName}_eq"
            nestedConditions.add(BinaryCondition.equalTo(customSql, CustomSql(":$eqSqlParamName")))
            paramSource.addValue(eqSqlParamName, it, fieldType)
        }

        primitiveFilterInput.neFilter?.let {
            val neSqlParamName = "${sqlParamName}_ne"
            nestedConditions.add(BinaryCondition.notEqualTo(customSql, CustomSql(":$neSqlParamName")))
            paramSource.addValue(neSqlParamName, it, fieldType)
        }

        primitiveFilterInput.gtFilter?.let {
            val gtSqlParamName = "${sqlParamName}_gt"
            nestedConditions.add(BinaryCondition.greaterThan(customSql, CustomSql(":$gtSqlParamName")))
            paramSource.addValue(gtSqlParamName, it, fieldType)
        }

        primitiveFilterInput.gteFilter?.let {
            val gteSqlParamName = "${sqlParamName}_gte"
            nestedConditions.add(BinaryCondition.greaterThanOrEq(customSql, CustomSql(":$gteSqlParamName")))
            paramSource.addValue(gteSqlParamName, it, fieldType)
        }

        primitiveFilterInput.ltFilter?.let {
            val ltSqlParamName = "${sqlParamName}_lt"
            nestedConditions.add(BinaryCondition.lessThan(customSql, CustomSql(":$ltSqlParamName")))
            paramSource.addValue(ltSqlParamName, it, fieldType)
        }

        primitiveFilterInput.lteFilter?.let {
            val lteSqlParamName = "${sqlParamName}_lte"
            nestedConditions.add(BinaryCondition.lessThanOrEq(customSql, CustomSql(":$lteSqlParamName")))
            paramSource.addValue(lteSqlParamName, it, fieldType)
        }

        primitiveFilterInput.betweenFilter?.let {
            val leftSqlParamName = "${sqlParamName}_left"
            val rightSqlParamName = "${sqlParamName}_right"
            nestedConditions.add(BetweenCondition(customSql, CustomSql(":$leftSqlParamName"), CustomSql(":$rightSqlParamName")))
            paramSource
                .addValue(leftSqlParamName, it.left, fieldType)
                .addValue(rightSqlParamName, it.right, fieldType)
        }

        primitiveFilterInput.inFilter?.let { list ->
            val arr = list.map { SQL.toSqlValue(it) }.toTypedArray()
            nestedConditions.add(InCondition(customSql, *arr))
        }

        primitiveFilterInput.notInFilter?.let { list ->
            val arr = list.map { SQL.toSqlValue(it) }.toTypedArray()
            nestedConditions.add(NotCondition(InCondition(customSql, *arr)))
        }

        if (primitiveFilterInput.nullFilter == true) {
            nestedConditions.add(UnaryCondition.isNull(customSql))
        }

        if (primitiveFilterInput.notNullFilter == true) {
            nestedConditions.add(UnaryCondition.isNotNull(customSql))
        }

        primitiveFilterInput.andFilterList?.let { list ->
            val andConditions =
                list.map {
                    newPrimitiveCondition(
                        dataset = dataset,
                        fields = fields,
                        fieldName = fieldName,
                        primitiveFilterInput = it,
                        customSql = customSql,
                        paramSource = paramSource,
                        fieldNumbers = fieldNumbers
                    )
                }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        primitiveFilterInput.orFilterList?.let { list ->
            val orConditions =
                list.map {
                    newPrimitiveCondition(
                        dataset = dataset,
                        fields = fields,
                        fieldName = fieldName,
                        primitiveFilterInput = it,
                        customSql = customSql,
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
                        fields = fields,
                        fieldName = fieldName,
                        primitiveFilterInput = it,
                        customSql = customSql,
                        paramSource = paramSource,
                        fieldNumbers = fieldNumbers
                    )
                )
            )
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }

    companion object {
        private val datasetSqlExprEvaluator = DatasetSqlExprEvaluator()
        private val nonWordRegex = "\\W".toRegex()
    }
}
