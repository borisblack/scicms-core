package ru.scisolutions.scicmscore.engine.schema.relation.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class OneToManyRelationHandler(
    private val relationValidator: RelationValidator,
    private val itemService: ItemService
) : RelationHandler {
    override fun getAttributeRelation(item: Item, attrName: String): OneToManyInversedBidirectionalRelation {
        relationValidator.validateAttribute(item, attrName)

        val attribute = item.spec.getAttributeOrThrow(attrName)
        val owningItem = itemService.getByName(requireNotNull(attribute.target))

        return OneToManyInversedBidirectionalRelation(
            owningItem = owningItem,
            owningAttrName = requireNotNull(attribute.mappedBy),
            inversedItem = item,
            inversedAttrName = attrName
        )
    }
}