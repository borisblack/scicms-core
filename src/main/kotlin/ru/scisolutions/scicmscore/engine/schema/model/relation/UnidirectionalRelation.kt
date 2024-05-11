package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.persistence.entity.Item

interface UnidirectionalRelation : Relation {
    val item: Item
    val attrName: String

    val targetItem: Item

    fun getTableName(): String = requireNotNull(item.tableName)
    fun getAttribute(): Attribute = item.spec.getAttribute(attrName)
}