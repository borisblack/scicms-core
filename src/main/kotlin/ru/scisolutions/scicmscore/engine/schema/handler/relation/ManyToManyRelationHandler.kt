package ru.scisolutions.scicmscore.engine.schema.handler.relation

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class ManyToManyRelationHandler(private val itemService: ItemService) : RelationHandler {
    override fun getAttributeRelation(item: Item, attrName: String, attribute: Attribute): ManyToManyBidirectionalRelation {
        requireNotNull(attribute.target) { "The [$attrName] attribute does not have a target field" }

        val targetItem = itemService.getItemOrThrow(attribute.target)

        // Validate attribute
        if (attribute.inversedBy != null && attribute.mappedBy != null)
            throw IllegalStateException("Attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

        if (attribute.inversedBy == null && attribute.mappedBy == null)
            throw IllegalStateException("Attribute does not have an inversedBy or mappedBy field, which is required for the manyToMany relationship")

        val intermediate = attribute.intermediate
            ?: throw IllegalStateException("Attribute does not have an intermediate field, which is required for the manyToMany relationship")

        // Get and validate intermediate item
        val intermediateItem = itemService.getItemOrThrow(intermediate)

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