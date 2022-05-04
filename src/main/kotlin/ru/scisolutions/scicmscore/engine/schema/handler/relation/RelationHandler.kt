package ru.scisolutions.scicmscore.engine.schema.handler.relation

import ru.scisolutions.scicmscore.engine.schema.model.relation.Relation
import ru.scisolutions.scicmscore.persistence.entity.Item

interface RelationHandler {
    fun getAttributeRelation(item: Item, attrName: String): Relation
}