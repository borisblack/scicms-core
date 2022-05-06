package ru.scisolutions.scicmscore.engine.schema.relation.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneOwningBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class ManyToOneRelationHandler(
    private val relationValidator: RelationValidator,
    private val itemService: ItemService
) : RelationHandler {
    override fun getAttributeRelation(item: Item, attrName: String): ManyToOneRelation {
        relationValidator.validateAttribute(item, attrName)

        val attribute = item.spec.getAttributeOrThrow(attrName)
        if (attribute.mappedBy != null)
            throw IllegalStateException("The mappedBy field cannot be set for manyToOne relation type")

        val targetItem = itemService.getItemOrThrow(requireNotNull(attribute.target))

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