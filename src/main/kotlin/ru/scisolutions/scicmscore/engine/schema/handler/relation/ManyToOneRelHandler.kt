package ru.scisolutions.scicmscore.engine.schema.handler.relation

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneOwningBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Component
class ManyToOneRelHandler(
    private val itemService: ItemService
) {
    fun getRelation(item: Item, attrName: String): ManyToOneRelation {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        if (attribute.mappedBy != null)
            throw IllegalStateException("The mappedBy field cannot be set for manyToOne relation type")

        val targetItemName = attribute.extractTarget()
        val targetItem = itemService.getItemOrThrow(targetItemName)

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