package ru.scisolutions.scicmscore.engine.schema.service

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.relation.Relation
import ru.scisolutions.scicmscore.persistence.entity.Item

interface RelationManager {
    fun getAttributeRelation(item: Item, attrName: String, attribute: Attribute): Relation
}