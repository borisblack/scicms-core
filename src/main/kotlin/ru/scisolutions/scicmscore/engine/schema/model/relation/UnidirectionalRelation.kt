package ru.scisolutions.scicmscore.engine.schema.model.relation

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item

abstract class UnidirectionalRelation(
    val item: Item,
    val attrName: String,

    val targetItem: Item
) : Relation {
    val tableName = item.tableName
    val attribute: Attribute = item.spec.getAttributeOrThrow(attrName)
}