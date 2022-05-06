package ru.scisolutions.scicmscore.engine.schema.relation

import ru.scisolutions.scicmscore.engine.schema.model.relation.Relation
import ru.scisolutions.scicmscore.persistence.entity.Item

interface RelationManager {
    fun getAttributeRelation(item: Item, attrName: String): Relation
}