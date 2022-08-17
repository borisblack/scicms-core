package ru.scisolutions.scicmscore.schema.model.relation

import ru.scisolutions.scicmscore.persistence.entity.Item

class ManyToManyUnidirectionalRelation(
    override val item: Item,
    override val attrName: String,

    override val targetItem: Item,
    override val intermediateItem: Item
) : UnidirectionalRelation, ManyToManyRelation