package ru.scisolutions.scicmscore.engine.service

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToOneOwningBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToOneRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToOneRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToOneUnidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.Relation
import ru.scisolutions.scicmscore.schema.service.RelationValidator

@Service
class RelationManager(
    private val relationValidator: RelationValidator,
    private val itemService: ItemService
) {
    fun getAttributeRelation(item: Item, attrName: String, attribute: Attribute): Relation {
        relationValidator.validateAttribute(item, attrName, attribute)

        return when (attribute.relType) {
            Attribute.RelType.oneToOne -> getOneToOneAttributeRelation(item, attrName, attribute)
            Attribute.RelType.manyToOne -> getManyToOneAttributeRelation(item, attrName, attribute)
            Attribute.RelType.oneToMany -> getOneToManyAttributeRelation(item, attrName, attribute)
            Attribute.RelType.manyToMany ->getManyToManyAttributeRelation(item, attrName, attribute)
            else -> throw IllegalArgumentException("Unsupported attribute relation type")
        }
    }

    private fun getOneToOneAttributeRelation(item: Item, attrName: String, attribute: Attribute): OneToOneRelation {
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

    private fun getManyToOneAttributeRelation(item: Item, attrName: String, attribute: Attribute): ManyToOneRelation {
        if (attribute.mappedBy != null)
            throw IllegalStateException("The mappedBy field cannot be set for manyToOne relation type")

        val targetItem = itemService.getByName(requireNotNull(attribute.target))

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

    private fun getOneToManyAttributeRelation(item: Item, attrName: String, attribute: Attribute): OneToManyInversedBidirectionalRelation {
        val owningItem = itemService.getByName(requireNotNull(attribute.target))

        return OneToManyInversedBidirectionalRelation(
            owningItem = owningItem,
            owningAttrName = requireNotNull(attribute.mappedBy),
            inversedItem = item,
            inversedAttrName = attrName
        )
    }

    private fun getManyToManyAttributeRelation(item: Item, attrName: String, attribute: Attribute): ManyToManyRelation {
        val targetItem = itemService.getByName(requireNotNull(attribute.target))
        val intermediateItem = itemService.getByName(requireNotNull(attribute.intermediate))

        // Create context
        return if (attribute.mappedBy == null && attribute.inversedBy == null) {
            ManyToManyUnidirectionalRelation(
                item = item,
                attrName = attrName,
                targetItem = targetItem,
                intermediateItem = intermediateItem
            )
        } else if (attribute.inversedBy != null) { // owning side
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
        } else {
            throw IllegalStateException("Illegal state for manyToMany context")
        }
    }
}