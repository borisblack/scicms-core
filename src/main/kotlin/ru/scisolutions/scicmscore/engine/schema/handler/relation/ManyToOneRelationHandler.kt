package ru.scisolutions.scicmscore.engine.schema.handler.relation

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneOwningBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class ManyToOneRelationHandler(private val itemService: ItemService) : RelationHandler {
    override fun getAttributeRelation(item: Item, attrName: String, attribute: Attribute): ManyToOneRelation {
        requireNotNull(attribute.target) { "The [$attrName] attribute does not have a target field" }

        if (attribute.mappedBy != null)
            throw IllegalStateException("The mappedBy field cannot be set for manyToOne relation type")

        val targetItem = itemService.getItemOrThrow(attribute.target)

        return if (attribute.inversedBy == null) {
            ManyToOneUnidirectionalRelation(
                item = item,
                attrName = attrName,
                targetItem = targetItem
            )
        } else {
            ManyToOneOwningBidirectionalRelation(
                owningItem = item,
                owningAttrName = attrName,
                inversedItem = targetItem,
                inversedAttrName = attribute.inversedBy
            )
        }
    }
}