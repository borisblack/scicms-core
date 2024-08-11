package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.engine.persistence.entity.Item

class OneToOneUnidirectionalRelation(
    override val item: Item,
    override val attrName: String,
    override val targetItem: Item,
) : UnidirectionalRelation, OneToOneRelation {
    fun getColumnName() = getAttribute().columnName ?: attrName.lowercase()
}
