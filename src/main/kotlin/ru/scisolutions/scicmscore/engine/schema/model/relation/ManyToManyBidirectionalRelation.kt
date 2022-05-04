package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.persistence.entity.Item

class ManyToManyBidirectionalRelation(
    val isOwning: Boolean,

    override val owningItem: Item,
    override val owningAttrName: String,

    override val inversedItem: Item,
    override val inversedAttrName: String,

    override val intermediateItem: Item
) : BidirectionalRelation, ManyToManyRelation