package ru.scisolutions.scicmscore.engine.schema.handler.relation

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Component
class ManyToManyRelHandler(
    private val itemService: ItemService
) {
    fun getRelation(item: Item, attrName: String): ManyToManyBidirectionalRelation {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        val targetItemName = attribute.extractTarget()
        val targetItem = itemService.getItemOrThrow(targetItemName)

        // Validate attribute
        if (attribute.inversedBy != null && attribute.mappedBy != null)
            throw IllegalStateException("Attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

        if (attribute.inversedBy == null && attribute.mappedBy == null)
            throw IllegalStateException("Attribute does not have an inversedBy or mappedBy field, which is required for the manyToMany relationship")

        val intermediate = attribute.intermediate
            ?: throw IllegalStateException("Attribute does not have an intermediate field, which is required for the manyToMany relationship")

        // Get and validate intermediate item
        val intermediateItem = itemService.getItemOrThrow(intermediate)

        val sourceIntermediateAttribute = intermediateItem.spec.getAttributeOrThrow(ManyToManyRelation.SOURCE_ATTR_NAME)
        if (sourceIntermediateAttribute.type != Attribute.Type.relation)
            throw IllegalStateException("The source attribute of intermediate item must be of relation type")

        if (sourceIntermediateAttribute.relType != Attribute.RelType.manyToOne)
            throw IllegalStateException("The source attribute of intermediate item must be of manyToOne relation type")

        val targetIntermediateAttribute = intermediateItem.spec.getAttributeOrThrow(ManyToManyRelation.TARGET_ATTR_NAME)
        if (targetIntermediateAttribute.type != Attribute.Type.relation)
            throw IllegalStateException("The target attribute of intermediate item must be of relation type")

        if (targetIntermediateAttribute.relType != Attribute.RelType.manyToOne)
            throw IllegalStateException("The target attribute of intermediate item must be of manyToOne relation type")

        // Create context
        return if (attribute.inversedBy != null) { // owning side
            val sourceIntermediateAttributeTarget = sourceIntermediateAttribute.extractTarget()
            if (sourceIntermediateAttributeTarget != item.name)
                throw IllegalArgumentException("Current item name and intermediate attribute target does not match")

            ManyToManyBidirectionalRelation(
                isOwning = true,
                owningItem = item,
                owningAttrName = attrName,
                inversedItem = targetItem,
                inversedAttrName = attribute.inversedBy,
                intermediateItem = intermediateItem,
                sourceIntermediateAttrName = ManyToManyRelation.SOURCE_ATTR_NAME,
                targetIntermediateAttrName = ManyToManyRelation.TARGET_ATTR_NAME
            )
        } else if (attribute.mappedBy != null) { // inversed side
            val targetIntermediateAttributeTarget = targetIntermediateAttribute.extractTarget()
            if (targetIntermediateAttributeTarget != item.name)
                throw IllegalArgumentException("Current item name and intermediate attribute target does not match")

            val mappedByAttribute = targetItem.spec.getAttributeOrThrow(attribute.mappedBy)

            ManyToManyBidirectionalRelation(
                isOwning = false,
                owningItem = targetItem,
                owningAttrName = attribute.mappedBy,
                inversedItem = item,
                inversedAttrName = attrName,
                intermediateItem = intermediateItem,
                sourceIntermediateAttrName = ManyToManyRelation.SOURCE_ATTR_NAME,
                targetIntermediateAttrName = ManyToManyRelation.TARGET_ATTR_NAME
            )
        } else
            throw IllegalStateException("Illegal state for manyToMany context")
    }
}