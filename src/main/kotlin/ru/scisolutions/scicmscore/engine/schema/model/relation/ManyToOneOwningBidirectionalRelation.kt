package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.engine.persistence.entity.Item

class ManyToOneOwningBidirectionalRelation(
    override val owningItem: Item,
    override val owningAttrName: String,

    override val inversedItem: Item,
    override val inversedAttrName: String
) : BidirectionalRelation, ManyToOneRelation {
    fun getOwningColumnName(): String = getOwningAttribute().columnName ?: owningAttrName.lowercase()
}