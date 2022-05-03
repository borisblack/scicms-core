package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item

abstract class BidirectionalRelation(
    val owningItem: Item,
    val owningAttrName: String,

    val inversedItem: Item,
    val inversedAttrName: String
) : Relation {
    val owningTableName: String = owningItem.tableName
    val owningAttribute: Attribute = owningItem.spec.getAttributeOrThrow(owningAttrName)

    val inversedTableName: String = inversedItem.tableName
    val inversedAttribute = inversedItem.spec.getAttributeOrThrow(inversedAttrName)
}