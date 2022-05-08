package ru.scisolutions.scicmscore.engine.schema.relation.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class ManyToManyRelationHandler(
    private val relationValidator: RelationValidator,
    private val itemService: ItemService
) : RelationHandler {
    override fun getAttributeRelation(item: Item, attrName: String): ManyToManyBidirectionalRelation {
        relationValidator.validateAttribute(item, attrName)

        val attribute = item.spec.getAttributeOrThrow(attrName)
        val targetItem = itemService.getByName(requireNotNull(attribute.target))
        val intermediateItem = itemService.getByName(requireNotNull(attribute.intermediate))

        // Create context
        return if (attribute.inversedBy != null) { // owning side
            ManyToManyBidirectionalRelation(
                isOwning = true,
                owningItem = item,
                owningAttrName = attrName,
                inversedItem = targetItem,
                inversedAttrName = attribute.inversedBy,
                intermediateItem = intermediateItem
            )
        } else if (attribute.mappedBy != null) { // inversed side
            ManyToManyBidirectionalRelation(
                isOwning = false,
                owningItem = targetItem,
                owningAttrName = attribute.mappedBy,
                inversedItem = item,
                inversedAttrName = attrName,
                intermediateItem = intermediateItem
            )
        } else
            throw IllegalStateException("Illegal state for manyToMany context")
    }
}