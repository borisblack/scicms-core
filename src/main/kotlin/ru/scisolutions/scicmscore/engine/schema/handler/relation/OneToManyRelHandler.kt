package ru.scisolutions.scicmscore.engine.schema.handler.relation

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Component
class OneToManyRelHandler(
    private val itemService: ItemService
) {
    fun getRelation(inversedItem: Item, attrName: String): OneToManyInversedBidirectionalRelation {
        val attribute = inversedItem.spec.getAttributeOrThrow(attrName)
        if (attribute.inversedBy != null && attribute.mappedBy != null)
            throw IllegalStateException("The [$attrName] attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

        val mappedBy = attribute.mappedBy
            ?: throw IllegalStateException("The [$attrName] attribute does not have a mappedBy field, which is required for the oneToMany relationship")

        val owningItemName = attribute.extractTarget()
        val owningItem = itemService.getItemOrThrow(owningItemName)

        return OneToManyInversedBidirectionalRelation(
            owningItem = owningItem,
            owningAttrName = mappedBy,
            inversedItem = inversedItem,
            inversedAttrName = attrName
        )
    }
}