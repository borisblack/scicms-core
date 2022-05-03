package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item

class OneToManyInversedBidirectionalRelation(
    owningItem: Item,
    owningAttrName: String,

    inversedItem: Item,
    inversedAttrName: String
) : BidirectionalRelation(owningItem, owningAttrName, inversedItem, inversedAttrName), OneToManyRelation {
    val owningColumnName: String = owningAttribute.columnName ?: owningAttrName.lowercase()

    val inversedKeyAttrName: String = owningAttribute.extractTargetKeyAttrName()
    val inversedKeyAttribute: Attribute = inversedItem.spec.getAttributeOrThrow(inversedKeyAttrName)
    val inversedKeyColumnName: String = inversedKeyAttribute.columnName ?: inversedKeyAttrName.lowercase()
}