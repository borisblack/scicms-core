package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.BetweenCondition
import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.NotCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.SelectQuery.JoinType
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.input.ItemFiltersInput
import ru.scisolutions.scicmscore.engine.model.input.TypedPrimitiveFilterInput
import ru.scisolutions.scicmscore.engine.service.RelationManager
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToOneOwningBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToOneUnidirectionalRelation
import kotlin.random.Random

@Component
class ItemFilterConditionBuilder(
    private val itemService: ItemService,
    private val relationManager: RelationManager
) {
    fun newFilterCondition(item: Item, itemFiltersInput: ItemFiltersInput, schema: DbSchema, table: DbTable, query: SelectQuery, paramSource: AttributeSqlParameterSource): Condition {
        val nestedConditions = mutableListOf<Condition>()

        itemFiltersInput.attributeFilters.forEach { (attrName, attrFilter) ->
            val attribute = item.spec.getAttribute(attrName)
            val target =
                when (attribute.type) {
                    FieldType.media -> MEDIA_ITEM_NAME
                    else -> attribute.target
                }

            if (attrFilter is ItemFiltersInput) {
                requireNotNull(target) { "The [$attrName] attribute does not have a target field." }

                val targetItem = itemService.getByName(target)
                val targetTable = DbTable(schema, requireNotNull(targetItem.tableName))
                val idCol = DbColumn(table, ID_COL_NAME, null, null)
                val targetIdCol = DbColumn(targetTable, ID_COL_NAME, null, null)
                val relation =
                    when (attribute.type) {
                        FieldType.media -> OneToOneUnidirectionalRelation(
                            item = item,
                            attrName = attrName,
                            targetItem = targetItem
                        )
                        else -> relationManager.getAttributeRelation(item, attrName, attribute)
                    }

                when (relation) {
                    is OneToOneUnidirectionalRelation -> {
                        val col = DbColumn(table, relation.getColumnName(), null, null)
                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(col, targetIdCol))
                    }
                    is OneToOneBidirectionalRelation -> {
                        if (relation.isOwning) {
                            val owningCol = DbColumn(table, relation.getOwningColumnName(), null, null)
                            query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(owningCol, targetIdCol))
                        } else {
                            val inversedCol = DbColumn(table, relation.getInversedColumnName(), null, null)
                            query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(inversedCol, targetIdCol))
                        }
                    }
                    is ManyToOneUnidirectionalRelation -> {
                        val col = DbColumn(table, relation.getColumnName(), null, null)
                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(col, targetIdCol))
                    }
                    is ManyToOneOwningBidirectionalRelation -> {
                        val owningCol = DbColumn(table, relation.getOwningColumnName(), null, null)
                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(owningCol, targetIdCol))
                    }
                    is OneToManyInversedBidirectionalRelation -> {
                        val owningCol = DbColumn(targetTable, relation.getOwningColumnName(), null, null)
                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(idCol, owningCol))
                    }
                    is ManyToManyRelation -> {
                        val intermediateTable = DbTable(schema, relation.getIntermediateTableName())
                        val sourceIntermediateCol = DbColumn(intermediateTable, relation.getIntermediateSourceColumnName(), null, null)
                        val targetIntermediateCol = DbColumn(intermediateTable, relation.getIntermediateTargetColumnName(), null, null)

                        when (relation) {
                            is ManyToManyUnidirectionalRelation -> {
                                query.addJoin(JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(idCol, sourceIntermediateCol))
                                query.addJoin(JoinType.LEFT_OUTER, intermediateTable, targetTable, BinaryCondition.equalTo(targetIntermediateCol, targetIdCol))
                            }
                            is ManyToManyBidirectionalRelation -> {
                                if (relation.isOwning) { // owning side
                                    query.addJoin(JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(idCol, sourceIntermediateCol))
                                    query.addJoin(JoinType.LEFT_OUTER, intermediateTable, targetTable, BinaryCondition.equalTo(targetIntermediateCol, targetIdCol))
                                } else { // inversed side
                                    query.addJoin(JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(idCol, targetIntermediateCol))
                                    query.addJoin(JoinType.LEFT_OUTER, intermediateTable, targetTable, BinaryCondition.equalTo(sourceIntermediateCol, targetIdCol))
                                }
                            }
                        }
                    }
                }
                nestedConditions.add(newFilterCondition(targetItem, attrFilter, schema, targetTable, query, paramSource))
            } else if (attrFilter is TypedPrimitiveFilterInput) {
                val column = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
                nestedConditions.add(newPrimitiveCondition(attrFilter, table, column, paramSource))
            }
        }

        itemFiltersInput.andFilterList?.let { list ->
            val andConditions = list.map { newFilterCondition(item, it, schema, table, query, paramSource) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        itemFiltersInput.orFilterList?.let { list ->
            val orConditions = list.map { newFilterCondition(item, it, schema, table, query, paramSource) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.OR, *orConditions.toTypedArray()))
        }

        itemFiltersInput.notFilter?.let {
            nestedConditions.add(NotCondition(newFilterCondition(item, it, schema, table, query, paramSource)))
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }

    private fun newPrimitiveCondition(typedPrimitiveFilterInput: TypedPrimitiveFilterInput, table: DbTable, column: DbColumn, paramSource: AttributeSqlParameterSource): Condition {
        val nestedConditions = mutableListOf<Condition>()
        val sqlParamName = "${table.alias}_${column.name}_${Random.nextInt(0, 1000)}" // TODO: Change to truly unique name

        typedPrimitiveFilterInput.containsFilter?.let {
            nestedConditions.add(BinaryCondition.like(column, "%$it%"))
        }

        typedPrimitiveFilterInput.containsiFilter?.let {
            nestedConditions.add(BinaryCondition.like(CustomSql("LOWER(${table.alias}.${column.name})"), "%${it.lowercase()}%"))
        }

        typedPrimitiveFilterInput.notContainsFilter?.let {
            nestedConditions.add(BinaryCondition.notLike(column, "%$it%"))
        }

        typedPrimitiveFilterInput.notContainsiFilter?.let {
            nestedConditions.add(BinaryCondition.notLike(CustomSql("LOWER(${table.alias}.${column.name})"), "%${it.lowercase()}%"))
        }

        typedPrimitiveFilterInput.startsWithFilter?.let {
            nestedConditions.add(BinaryCondition.like(column, "$it%"))
        }

        typedPrimitiveFilterInput.endsWithFilter?.let {
            nestedConditions.add(BinaryCondition.like(column, "%$it"))
        }

        typedPrimitiveFilterInput.eqFilter?.let {
            val eqSqlParamName = "${sqlParamName}_eq"
            nestedConditions.add(BinaryCondition.equalTo(column, CustomSql(":$eqSqlParamName")))
            paramSource.addValue(eqSqlParamName, it, typedPrimitiveFilterInput.attrType)
        }

        typedPrimitiveFilterInput.neFilter?.let {
            val neSqlParamName = "${sqlParamName}_ne"
            nestedConditions.add(BinaryCondition.notEqualTo(column, CustomSql(":$neSqlParamName")))
            paramSource.addValue(neSqlParamName, it, typedPrimitiveFilterInput.attrType)
        }

        typedPrimitiveFilterInput.gtFilter?.let {
            val gtSqlParamName = "${sqlParamName}_gt"
            nestedConditions.add(BinaryCondition.greaterThan(column, CustomSql(":$gtSqlParamName")))
            paramSource.addValue(gtSqlParamName, it, typedPrimitiveFilterInput.attrType)
        }

        typedPrimitiveFilterInput.gteFilter?.let {
            val gteSqlParamName = "${sqlParamName}_gte"
            nestedConditions.add(BinaryCondition.greaterThanOrEq(column, CustomSql(":$gteSqlParamName")))
            paramSource.addValue(gteSqlParamName, it, typedPrimitiveFilterInput.attrType)
        }

        typedPrimitiveFilterInput.ltFilter?.let {
            val ltSqlParamName = "${sqlParamName}_lt"
            nestedConditions.add(BinaryCondition.lessThan(column, CustomSql(":$ltSqlParamName")))
            paramSource.addValue(ltSqlParamName, it, typedPrimitiveFilterInput.attrType)
        }

        typedPrimitiveFilterInput.lteFilter?.let {
            val lteSqlParamName = "${sqlParamName}_lte"
            nestedConditions.add(BinaryCondition.lessThanOrEq(column, CustomSql(":$lteSqlParamName")))
            paramSource.addValue(lteSqlParamName, it, typedPrimitiveFilterInput.attrType)
        }

        typedPrimitiveFilterInput.betweenFilter?.let {
            val leftSqlParamName = "${sqlParamName}_left"
            val rightSqlParamName = "${sqlParamName}_right"
            nestedConditions.add(BetweenCondition(column, CustomSql(":$leftSqlParamName"), CustomSql(":$rightSqlParamName")))
            paramSource.addValue(leftSqlParamName, it.left, typedPrimitiveFilterInput.attrType)
            paramSource.addValue(rightSqlParamName, it.right, typedPrimitiveFilterInput.attrType)
        }

        typedPrimitiveFilterInput.inFilter?.let { list ->
            val arr = list.map { SQL.toSqlValue(it) }.toTypedArray()
            nestedConditions.add(InCondition(column, *arr))
        }

        typedPrimitiveFilterInput.notInFilter?.let { list ->
            val arr = list.map { SQL.toSqlValue(it) }.toTypedArray()
            nestedConditions.add(NotCondition(InCondition(column, *arr)))
        }

        if (typedPrimitiveFilterInput.nullFilter == true) {
            nestedConditions.add(UnaryCondition.isNull(column))
        }

        if (typedPrimitiveFilterInput.notNullFilter == true) {
            nestedConditions.add(UnaryCondition.isNotNull(column))
        }

        typedPrimitiveFilterInput.andFilterList?.let { list ->
            val andConditions = list.map { newPrimitiveCondition(it, table, column, paramSource) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        typedPrimitiveFilterInput.orFilterList?.let { list ->
            val orConditions = list.map { newPrimitiveCondition(it, table, column, paramSource) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.OR, *orConditions.toTypedArray()))
        }

        typedPrimitiveFilterInput.notFilter?.let {
            nestedConditions.add(NotCondition(newPrimitiveCondition(it, table, column, paramSource)))
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }

    companion object {
        private const val MEDIA_ITEM_NAME = "media"
        private const val ID_COL_NAME = "id"
    }
}