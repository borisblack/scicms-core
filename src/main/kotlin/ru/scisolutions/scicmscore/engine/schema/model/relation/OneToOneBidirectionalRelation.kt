package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.persistence.entity.Item

class OneToOneBidirectionalRelation(
    val isOwning: Boolean,

    override val owningItem: Item,
    override val owningAttrName: String,

    override val inversedItem: Item,
    override val inversedAttrName: String
) : BidirectionalRelation, OneToOneRelation {
    fun getOwningColumnName(): String = getOwningAttribute().columnName ?: owningAttrName.lowercase()

    fun getInversedColumnName(): String = getInversedAttribute().columnName ?: inversedAttrName.lowercase()
}