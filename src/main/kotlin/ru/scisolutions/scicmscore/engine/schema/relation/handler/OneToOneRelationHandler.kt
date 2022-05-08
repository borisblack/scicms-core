package ru.scisolutions.scicmscore.engine.schema.relation.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class OneToOneRelationHandler(
    private val relationValidator: RelationValidator,
    private val itemService: ItemService
) : RelationHandler {
    override fun getAttributeRelation(item: Item, attrName: String): OneToOneRelation {
        relationValidator.validateAttribute(item, attrName)

        val attribute = item.spec.getAttributeOrThrow(attrName)
        val targetItem = itemService.getByName(requireNotNull(attribute.target))

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