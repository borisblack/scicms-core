package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.persistence.entity.Item

class ManyToOneUnidirectionalRelation(
    override val item: Item,
    override val attrName: String,

    override val targetItem: Item
) : UnidirectionalRelation, ManyToOneRelation {
    fun getColumnName() = getAttribute().columnName ?: attrName.lowercase()
}