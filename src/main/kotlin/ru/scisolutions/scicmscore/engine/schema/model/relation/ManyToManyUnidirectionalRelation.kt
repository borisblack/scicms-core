package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.engine.persistence.entity.Item

class ManyToManyUnidirectionalRelation(
    override val item: Item,
    override val attrName: String,
    override val targetItem: Item,
    override val intermediateItem: Item,
) : UnidirectionalRelation, ManyToManyRelation
