package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.persistence.entity.Item

class ManyToOneOwningBidirectionalRelation(
    owningItem: Item,
    owningAttrName: String,

    inversedItem: Item,
    inversedAttrName: String
) : BidirectionalRelation(owningItem, owningAttrName, inversedItem, inversedAttrName), ManyToOneRelation {
    val owningColumnName: String = owningAttribute.columnName ?: owningAttrName.lowercase()

    val inversedKeyAttrName = owningAttribute.extractTargetKeyAttrName()
    val inversedKeyAttribute = inversedItem.spec.getAttributeOrThrow(inversedKeyAttrName)
    val inversedKeyColumnName = inversedKeyAttribute.columnName ?: inversedKeyAttrName.lowercase()
}