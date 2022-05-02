package ru.scisolutions.scicmscore.engine.data.db

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
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.model.input.ItemFiltersInput
import ru.scisolutions.scicmscore.engine.data.model.input.PrimitiveFilterInput
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Component
class ConditionBuilder(
    private val itemService: ItemService
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
                when (attribute.relType) {
                    RelType.oneToOne, RelType.manyToOne -> {
                        val thisColumn = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
                        val targetKeyAttrName = attribute.extractTargetKeyAttrName()
                        val targetKeyAttribute = targetItem.spec.getAttributeOrThrow(targetKeyAttrName)
                        val targetColumn = DbColumn(targetTable, targetKeyAttribute.columnName ?: targetKeyAttrName.lowercase(), null, null)
                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(thisColumn, targetColumn))
                    }
                    RelType.oneToMany -> {
                        if (attribute.inversedBy != null && attribute.mappedBy != null)
                            throw IllegalStateException("The [$attrName] attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

                        val mappedBy = attribute.mappedBy
                            ?: throw IllegalStateException("The [$attrName] attribute does not have a mappedBy field, which is required for the oneToMany relationship")

                        val mappedByAttribute = targetItem.spec.getAttributeOrThrow(mappedBy)
                        val targetColumn = DbColumn(targetTable, mappedByAttribute.columnName ?: mappedBy.lowercase(), null, null)
                        val thisKeyAttrName = mappedByAttribute.extractTargetKeyAttrName()
                        val thisKeyAttribute = item.spec.getAttributeOrThrow(thisKeyAttrName)
                        val thisColumn = DbColumn(table, thisKeyAttribute.columnName ?: thisKeyAttrName.lowercase(), null, null)
                        query.addJoin(JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(thisColumn, targetColumn))
                    }
                    RelType.manyToMany -> {
                        if (attribute.inversedBy != null && attribute.mappedBy != null)
                            throw IllegalStateException("The [$attrName] attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

                        if (attribute.inversedBy == null && attribute.mappedBy == null)
                            throw IllegalStateException("The [$attrName] attribute does not have an inversedBy or mappedBy field, which is required for the manyToMany relationship")

                        val intermediate = attribute.intermediate
                            ?: throw IllegalStateException("The [$attrName] attribute does not have an intermediate field, which is required for the manyToMany relationship")

                        val intermediateItem = itemService.getItemOrThrow(intermediate)
                        val intermediateTable = DbTable(schema, intermediateItem.tableName)

                        val sourceIntermediateAttribute = intermediateItem.spec.getAttributeOrThrow(SOURCE_ATTR_NAME)
                        if (sourceIntermediateAttribute.type != Type.relation)
                            throw IllegalStateException("The source attribute of intermediate item must be of relation type")

                        if (sourceIntermediateAttribute.relType != RelType.manyToOne)
                            throw IllegalStateException("The source attribute of intermediate item must be of manyToOne relation type")

                        val targetIntermediateAttribute = intermediateItem.spec.getAttributeOrThrow(TARGET_ATTR_NAME)

                        if (targetIntermediateAttribute.type != Type.relation)
                            throw IllegalStateException("The target attribute of intermediate item must be of relation type")

                        if (targetIntermediateAttribute.relType != RelType.manyToOne)
                            throw IllegalStateException("The target attribute of intermediate item must be of manyToOne relation type")

                        val sourceIntermediateCol = DbColumn(intermediateTable, sourceIntermediateAttribute.columnName ?: SOURCE_ATTR_NAME.lowercase(), null, null)
                        val targetIntermediateCol = DbColumn(intermediateTable, targetIntermediateAttribute.columnName ?: TARGET_ATTR_NAME.lowercase(), null, null)

                        if (attribute.inversedBy != null) { // owning side
                            val sourceIntermediateAttributeTarget = sourceIntermediateAttribute.extractTarget()
                            if (sourceIntermediateAttributeTarget != item.name)
                                throw IllegalArgumentException("Current item name and intermediate attribute target does not match")

                            val thisKeyAttrName = sourceIntermediateAttribute.extractTargetKeyAttrName()
                            val thisKeyAttribute = item.spec.getAttributeOrThrow(thisKeyAttrName)
                            val thisColumn = DbColumn(table, thisKeyAttribute.columnName ?: thisKeyAttrName.lowercase(), null, null)
                            query.addJoin(JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(thisColumn, sourceIntermediateCol))

                            val inversedByAttribute = targetItem.spec.getAttributeOrThrow(attribute.inversedBy)
                            val targetColumn = DbColumn(targetTable, inversedByAttribute.columnName ?: attribute.inversedBy.lowercase(), null, null)
                            query.addJoin(JoinType.LEFT_OUTER, intermediateTable, targetTable, BinaryCondition.equalTo(targetIntermediateCol, targetColumn))
                        } else if (attribute.mappedBy != null) { // relation side
                            val targetIntermediateAttributeTarget = targetIntermediateAttribute.extractTarget()
                            if (targetIntermediateAttributeTarget != item.name)
                                throw IllegalArgumentException("Current item name and intermediate attribute target does not match")

                            val thisKeyAttrName = targetIntermediateAttribute.extractTargetKeyAttrName()
                            val thisKeyAttribute = item.spec.getAttributeOrThrow(thisKeyAttrName)
                            val thisColumn = DbColumn(table, thisKeyAttribute.columnName ?: thisKeyAttrName.lowercase(), null, null)
                            query.addJoin(JoinType.LEFT_OUTER, table, intermediateTable, BinaryCondition.equalTo(thisColumn, targetIntermediateCol))

                            val mappedByAttribute = targetItem.spec.getAttributeOrThrow(attribute.mappedBy)
                            val targetColumn = DbColumn(targetTable, mappedByAttribute.columnName ?: attribute.mappedBy.lowercase(), null, null)
                            query.addJoin(JoinType.LEFT_OUTER, intermediateTable, targetTable, BinaryCondition.equalTo(sourceIntermediateCol, targetColumn))
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

    companion object {
        private const val SOURCE_ATTR_NAME = "source"
        private const val TARGET_ATTR_NAME = "target"
    }
}