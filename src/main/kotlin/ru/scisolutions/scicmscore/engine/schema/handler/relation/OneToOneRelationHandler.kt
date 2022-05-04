package ru.scisolutions.scicmscore.engine.schema.handler.relation

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class OneToOneRelationHandler(private val itemService: ItemService) : RelationHandler {
    override fun getAttributeRelation(item: Item, attrName: String): OneToOneRelation {
        val attribute = item.spec.getAttributeOrThrow(attrName)

        requireNotNull(attribute.target) { "The [$attrName] attribute does not have a target field" }

        val targetItem = itemService.getItemOrThrow(attribute.target)

        // Validate attribute
        if (attribute.inversedBy != null && attribute.mappedBy != null)
            throw IllegalStateException("Attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

        // Create context
        return if (attribute.inversedBy != null) { // owning side
            OneToOneBidirectionalRelation(
                isOwning = true,
                owningItem = item,
                owningAttrName = attrName,
                inversedItem = targetItem,
                inversedAttrName = attribute.inversedBy
            )
        } else if (attribute.mappedBy != null) { // inversed side
            OneToOneBidirectionalRelation(
                isOwning = false,
                owningItem = targetItem,
                owningAttrName = attribute.mappedBy,
                inversedItem = item,
                inversedAttrName = attrName
            )
        } else {
            OneToOneUnidirectionalRelation(
                item = item,
                attrName = attrName,
                targetItem = targetItem
            )
        }
    }
}