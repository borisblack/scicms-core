package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.engine.persistence.entity.Item

class OneToManyInversedBidirectionalRelation(
    override val owningItem: Item,
    override val owningAttrName: String,
    override val inversedItem: Item,
    override val inversedAttrName: String
) : BidirectionalRelation, OneToManyRelation {
    fun getOwningColumnName(): String = getOwningAttribute().columnName ?: owningAttrName.lowercase()
}
