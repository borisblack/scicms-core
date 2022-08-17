package ru.scisolutions.scicmscore.engine.service

import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.schema.model.relation.Relation

interface RelationManager {
    fun getAttributeRelation(item: Item, attrName: String, attribute: Attribute): Relation
}