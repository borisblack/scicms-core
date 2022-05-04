package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item

interface UnidirectionalRelation : Relation {
    val item: Item
    val attrName: String

    val targetItem: Item

    fun getTableName(): String = item.tableName
    fun getAttribute(): Attribute = item.spec.getAttributeOrThrow(attrName)
}