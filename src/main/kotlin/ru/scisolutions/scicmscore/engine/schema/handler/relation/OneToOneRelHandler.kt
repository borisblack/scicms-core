package ru.scisolutions.scicmscore.engine.schema.handler.relation

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Component
class OneToOneRelHandler(
    private val itemService: ItemService
) {
    fun getRelation(item: Item, attrName: String): OneToOneRelation {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        val targetItemName = attribute.extractTarget()
        val targetItem = itemService.getItemOrThrow(targetItemName)

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