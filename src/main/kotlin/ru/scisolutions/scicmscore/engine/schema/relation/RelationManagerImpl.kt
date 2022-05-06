package ru.scisolutions.scicmscore.engine.schema.relation

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.model.relation.Relation
import ru.scisolutions.scicmscore.engine.schema.relation.handler.ManyToManyRelationHandler
import ru.scisolutions.scicmscore.engine.schema.relation.handler.ManyToOneRelationHandler
import ru.scisolutions.scicmscore.engine.schema.relation.handler.OneToManyRelationHandler
import ru.scisolutions.scicmscore.engine.schema.relation.handler.OneToOneRelationHandler
import ru.scisolutions.scicmscore.persistence.entity.Item

@Service
class RelationManagerImpl(
    private val oneToOneRelationHandler: OneToOneRelationHandler,
    private val manyToOneRelationHandler: ManyToOneRelationHandler,
    private val oneToManyRelationHandler: OneToManyRelationHandler,
    private val manyToManyRelationHandler: ManyToManyRelationHandler
) : RelationManager {
    override fun getAttributeRelation(item: Item, attrName: String): Relation {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        if (attribute.type != Type.relation)
            throw IllegalArgumentException("Attribute [$attrName] must be of relation type")

        requireNotNull(attribute.relType) { "The [$attrName] attribute does not have a relType field" }

        return when (attribute.relType) {
            RelType.oneToOne -> oneToOneRelationHandler.getAttributeRelation(item, attrName)
            RelType.manyToOne -> manyToOneRelationHandler.getAttributeRelation(item, attrName)
            RelType.oneToMany -> oneToManyRelationHandler.getAttributeRelation(item, attrName)
            RelType.manyToMany -> manyToManyRelationHandler.getAttributeRelation(item, attrName)
        }
    }
}