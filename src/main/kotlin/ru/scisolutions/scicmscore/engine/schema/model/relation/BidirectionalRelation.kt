package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.persistence.entity.Item

interface BidirectionalRelation : Relation {
    val owningItem: Item
    val owningAttrName: String

    val inversedItem: Item
    val inversedAttrName: String

    fun getOwningTableName(): String = requireNotNull(owningItem.tableName)

    fun getOwningAttribute(): Attribute = owningItem.spec.getAttribute(owningAttrName)

    fun getInversedTableName(): String = requireNotNull(inversedItem.tableName)

    fun getInversedAttribute(): Attribute = inversedItem.spec.getAttribute(inversedAttrName)
}
