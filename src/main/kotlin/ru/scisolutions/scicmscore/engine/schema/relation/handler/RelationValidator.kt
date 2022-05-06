package ru.scisolutions.scicmscore.engine.schema.relation.handler

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Component
class RelationValidator(private val itemService: ItemService) {
    fun validateAttribute(item: Item, attrName: String) {
        val attribute = item.spec.getAttributeOrThrow(attrName)

        requireNotNull(attribute.target) { "The [$attrName] attribute does not have a target field" }

        if (attribute.inversedBy != null && attribute.mappedBy != null)
            throw IllegalStateException("The [$attrName] attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

        val targetItem = itemService.getItemOrThrow(attribute.target)
        if (item.dataSource != targetItem.dataSource)
            throw IllegalStateException("Item [${item.name}] and it's attribute target item have different data sources")
    }
}