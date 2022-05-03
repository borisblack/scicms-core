package ru.scisolutions.scicmscore.engine.data.db.query

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
import ru.scisolutions.scicmscore.engine.data.model.input.ItemFiltersInput
import ru.scisolutions.scicmscore.engine.data.model.input.PrimitiveFilterInput
import ru.scisolutions.scicmscore.engine.schema.SchemaEngine
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneOwningBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Component
class ConditionBuilder(
    private val itemService: ItemService,
    private val schemaEngine: SchemaEngine
) {
    fun newItemCondition(schema: DbSchema, table: DbTable, query: SelectQuery, item: Item, itemFiltersInput: ItemFiltersInput
    ): Condition {
        val nestedConditions = mutableListOf<Condition>()

        itemFiltersInput.attributeFilters.forEach { (attrName, attrFilter) ->
            val attribute = item.spec.getAttributeOrThrow(attrName)
            if (attrFilter is ItemFiltersInput) {
                requireNotNull(attribute.relType) { "The [$attrName] attribute does not have a relType field" }

                val targetItemName = attribute.extractTarget()
                val targetItem = itemService.getItemOrThrow(targetItemName)
                val targetTable = DbTable(schema, targetItem.tableName)
                when (val relation = schemaEngine.getAttributeRelation(item, attrName)) {
                    is OneToOneBidirectionalRelation -> {
                        if (relation.isOwning) {
                            val owningColumn = DbColumn(table, relation.owningColumnName, null, null)
                            val inversedColumn = DbColumn(targetTable, relation.inversedColumnName, null, null)
                            query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(owningColumn, inversedColumn))
                        } else {
                            val inversedColumn = DbColumn(table, relation.inversedColumnName, null, null)
                            val owningColumn = DbColumn(targetTable, relation.owningColumnName, null, null)
                            query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(inversedColumn, owningColumn))
                        }
                    }
                    is OneToOneUnidirectionalRelation -> {
                        val thisColumn = DbColumn(table, relation.tableName, null, null)
                        val targetColumn = DbColumn(table, relation.targetKeyColumnName, null, null)
                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(thisColumn, targetColumn))
                    }
                    is ManyToOneUnidirectionalRelation -> {
                        val thisColumn = DbColumn(table, relation.columnName, null, null)
                        val targetColumn = DbColumn(targetTable, relation.targetKeyColumn, null, null)

                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(thisColumn, targetColumn))
                    }
                    is ManyToOneOwningBidirectionalRelation -> {
                        val owningColumn = DbColumn(table, relation.owningColumnName, null, null)
                        val inversedColumn = DbColumn(targetTable, relation.inversedKeyColumnName, null, null)

                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(owningColumn, inversedColumn))
                    }
                    is OneToManyInversedBidirectionalRelation -> {
                        val inversedColumn = DbColumn(table, relation.inversedKeyColumnName, null, null)
                        val owningColumn = DbColumn(targetTable, relation.owningColumnName, null, null)

                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(inversedColumn, owningColumn))
                    }
                    is ManyToManyBidirectionalRelation -> {
                        val intermediateTable = DbTable(schema, relation.intermediateTableName)
                        val sourceIntermediateCol = DbColumn(intermediateTable, relation.sourceIntermediateColumnName, null, null)
                        val targetIntermediateCol = DbColumn(intermediateTable, relation.targetIntermediateColumnName, null, null)

                        if (relation.isOwning) { // owning side
                            val owningColumn = DbColumn(table, relation.owningKeyColumnName, null, null)
                            query.addJoin(JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(owningColumn, sourceIntermediateCol))

                            val inversedColumn = DbColumn(targetTable, relation.inversedKeyColumnName, null, null)
                            query.addJoin(JoinType.LEFT_OUTER, intermediateTable, targetTable, BinaryCondition.equalTo(targetIntermediateCol, inversedColumn))
                        } else { // inversed side
                            val inversedColumn = DbColumn(table, relation.inversedKeyColumnName, null, null)
                            query.addJoin(JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(inversedColumn, targetIntermediateCol))

                            val owningColumn = DbColumn(targetTable, relation.owningKeyColumnName, null, null)
                            query.addJoin(JoinType.LEFT_OUTER, intermediateTable, targetTable, BinaryCondition.equalTo(sourceIntermediateCol, owningColumn))
                        }
                    }
                }
                nestedConditions.add(newItemCondition(schema, targetTable, query, targetItem, attrFilter))
            } else if (attrFilter is PrimitiveFilterInput) {
                val column = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
                nestedConditions.add(newPrimitiveCondition(table, column, attrFilter))
            }
        }

        itemFiltersInput.andFilterList?.let { list ->
            val andConditions = list.map { newItemCondition(schema, table, query, item, it) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        itemFiltersInput.orFilterList?.let { list ->
            val orConditions = list.map { newItemCondition(schema, table, query, item, it) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.OR, *orConditions.toTypedArray()))
        }

        itemFiltersInput.notFilter?.let {
            nestedConditions.add(NotCondition(newItemCondition(schema, table, query, item, it)))
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }

    private fun newPrimitiveCondition(table: DbTable, column: DbColumn, primitiveFilterInput: PrimitiveFilterInput): Condition {
        val nestedConditions = mutableListOf<Condition>()

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
            nestedConditions.add(BinaryCondition.equalTo(column, it))
        }

        primitiveFilterInput.neFilter?.let {
            nestedConditions.add(BinaryCondition.notEqualTo(column, it))
        }

        primitiveFilterInput.gtFilter?.let {
            nestedConditions.add(BinaryCondition.greaterThan(column, it))
        }

        primitiveFilterInput.gteFilter?.let {
            nestedConditions.add(BinaryCondition.greaterThanOrEq(column, it))
        }

        primitiveFilterInput.ltFilter?.let {
            nestedConditions.add(BinaryCondition.lessThan(column, it))
        }

        primitiveFilterInput.lteFilter?.let {
            nestedConditions.add(BinaryCondition.lessThanOrEq(column, it))
        }

        primitiveFilterInput.betweenFilter?.let {
            nestedConditions.add(BetweenCondition(column, it.left, it.right))
        }

        primitiveFilterInput.inFilter?.let {
            nestedConditions.add(InCondition(column, *it.toTypedArray()))
        }

        primitiveFilterInput.notInFilter?.let {
            nestedConditions.add(NotCondition(InCondition(column, *it.toTypedArray())))
        }

        if (primitiveFilterInput.nullFilter == true) {
            nestedConditions.add(UnaryCondition.isNull(column))
        }

        if (primitiveFilterInput.notNullFilter == true) {
            nestedConditions.add(UnaryCondition.isNotNull(column))
        }

        primitiveFilterInput.andFilterList?.let { list ->
            val andConditions = list.map { newPrimitiveCondition(table, column, it) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.AND, *andConditions.toTypedArray()))
        }

        primitiveFilterInput.orFilterList?.let { list ->
            val orConditions = list.map { newPrimitiveCondition(table, column, it) }
            nestedConditions.add(ComboCondition(ComboCondition.Op.OR, *orConditions.toTypedArray()))
        }

        primitiveFilterInput.notFilter?.let {
            nestedConditions.add(NotCondition(newPrimitiveCondition(table, column, it)))
        }

        return if (nestedConditions.isEmpty()) Condition.EMPTY else ComboCondition(ComboCondition.Op.AND, *nestedConditions.toTypedArray())
    }
}