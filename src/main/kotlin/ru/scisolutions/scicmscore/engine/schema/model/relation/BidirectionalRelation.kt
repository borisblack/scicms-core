package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item

interface BidirectionalRelation : Relation {
    val owningItem: Item
    val owningAttrName: String

    val inversedItem: Item
    val inversedAttrName: String

    fun getOwningTableName(): String = owningItem.tableName

    fun getOwningAttribute(): Attribute = owningItem.spec.getAttributeOrThrow(owningAttrName)

    fun getInversedTableName(): String = inversedItem.tableName

    fun getInversedAttribute(): Attribute = inversedItem.spec.getAttributeOrThrow(inversedAttrName)
}