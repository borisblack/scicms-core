package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.persistence.entity.Item

class ManyToOneUnidirectionalRelation(
    item: Item,
    attrName: String,

    targetItem: Item
) : UnidirectionalRelation(item, attrName, targetItem), ManyToOneRelation {
    val columnName = attribute.columnName ?: attrName.lowercase()

    val targetTableName = targetItem.tableName
    val targetKeyAttrName = attribute.extractTargetKeyAttrName()
    val targetKeyAttribute = targetItem.spec.getAttributeOrThrow(targetKeyAttrName)
    val targetKeyColumn = targetKeyAttribute.columnName ?: targetKeyAttrName.lowercase()
}