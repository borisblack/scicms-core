package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.persistence.entity.Item

class OneToOneBidirectionalRelation(
    val isOwning: Boolean,

    owningItem: Item,
    owningAttrName: String,

    inversedItem: Item,
    inversedAttrName: String
) : BidirectionalRelation(owningItem, owningAttrName, inversedItem, inversedAttrName), OneToOneRelation {
    val owningColumnName: String = owningAttribute.columnName ?: owningAttrName.lowercase()

    val inversedColumnName: String = inversedAttribute.columnName ?: inversedAttrName.lowercase()
}