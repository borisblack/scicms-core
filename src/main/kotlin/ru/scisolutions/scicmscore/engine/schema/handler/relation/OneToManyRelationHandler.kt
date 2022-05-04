package ru.scisolutions.scicmscore.engine.schema.handler.relation

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class OneToManyRelationHandler(private val itemService: ItemService) : RelationHandler {
    override fun getAttributeRelation(item: Item, attrName: String): OneToManyInversedBidirectionalRelation {
        val attribute = item.spec.getAttributeOrThrow(attrName)

        requireNotNull(attribute.target) { "The [$attrName] attribute does not have a target field" }

        if (attribute.inversedBy != null && attribute.mappedBy != null)
            throw IllegalStateException("The [$attrName] attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

        val mappedBy = attribute.mappedBy
            ?: throw IllegalStateException("The [$attrName] attribute does not have a mappedBy field, which is required for the oneToMany relationship")

        val owningItem = itemService.getItemOrThrow(attribute.target)

        return OneToManyInversedBidirectionalRelation(
            owningItem = owningItem,
            owningAttrName = mappedBy,
            inversedItem = item,
            inversedAttrName = attrName
        )
    }
}