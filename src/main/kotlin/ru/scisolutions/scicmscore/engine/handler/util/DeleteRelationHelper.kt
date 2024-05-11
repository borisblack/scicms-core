package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput.DeletingStrategy
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.RelationManager
import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.model.Attribute.RelType
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation

@Component
class DeleteRelationHelper(
    private val itemService: ItemService,
    private val relationManager: RelationManager,
    private val auditManager: AuditManager,
    private val deleteMediaHelper: DeleteMediaHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) {
    fun processRelations(item: Item, itemRec: ItemRec, strategy: DeletingStrategy) {
        processOneToOneRelations(item, itemRec, strategy)
        processCollectionRelations(item, requireNotNull(itemRec.id), strategy)
    }

    private fun processOneToOneRelations(item: Item, itemRec: ItemRec, strategy: DeletingStrategy) {
        if (strategy == DeletingStrategy.NO_ACTION)
            return

        val oneToOneRelAttributes = itemRec
            .filterKeys {
                val attribute = item.spec.getAttribute(it)
                attribute.type == FieldType.relation && attribute.relType == RelType.oneToOne
            }
            .filterValues { it != null } as Map<String, String>


        oneToOneRelAttributes.forEach { (attrName, targetId) ->
            processOneToOneRelation(item, attrName, targetId, strategy)
        }
    }

    private fun processOneToOneRelation(item: Item, relAttrName: String, targetId: String, strategy: DeletingStrategy) {
        logger.debug("Processing oneToOne relations")

        if (strategy == DeletingStrategy.NO_ACTION)
            return

        val attribute = item.spec.getAttribute(relAttrName)
        when (val relation = relationManager.getAttributeRelation(item, relAttrName, attribute)) {
            is OneToOneBidirectionalRelation -> {
                when (strategy) {
                    DeletingStrategy.SET_NULL -> {
                        if (relation.isOwning) {
                            if (relation.getInversedAttribute().required)
                                throw IllegalStateException("The [${relation.inversedAttrName}] is required in item [${relation.inversedItem.name}], so it cannot be cleared.")

                            val inversedItemRec = ItemRec(mutableMapOf(relation.inversedAttrName to null))
                            updateById(relation.inversedItem, targetId, inversedItemRec)
                        } else {
                            if (relation.getOwningAttribute().required)
                                throw IllegalStateException("The [${relation.owningAttrName}] is required in item [${relation.owningItem.name}], so it cannot be cleared.")

                            val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to null))
                            updateById(relation.owningItem, targetId, owningItemRec)
                        }
                    }
                    DeletingStrategy.CASCADE -> {
                        val targetItem = itemService.getByName(requireNotNull(attribute.target))
                        val targetItemRec = aclItemRecDao.findByIdForDelete(targetItem, targetId)
                        if (targetItemRec == null) {
                            logger.warn("Delete operation disabled for item [${targetItem.name}] with ID [$targetId]")
                        } else {
                            logger.debug("Processing relations recursively")

                            processRelations(targetItem, targetItemRec, strategy)

                            // Can be used by another versions or localizations
                            if (!item.versioned && !item.localized) {
                                deleteMediaHelper.processMedia(item, targetItemRec)
                            }

                            deleteById(targetItem, targetId)
                        }
                    }
                    else -> {}
                }
            }
            else -> {}
        }
    }

    private fun updateById(item: Item, id: String, itemRec: ItemRec): Int {
        if (!aclItemRecDao.existsByIdForWrite(item, id)) {
            logger.warn("Update operation disabled for item [${item.name}] with ID [$id].")
            return 0
        }

        auditManager.assignUpdateAttributes(itemRec)
        return itemRecDao.updateById(item, id, itemRec)
    }

    private fun updateByAttribute(item: Item, attrName: String, attrValue: Any, itemRec: ItemRec): Int {
        auditManager.assignUpdateAttributes(itemRec)
        val itemsToUpdate = aclItemRecDao.findAllByAttributeForWrite(item, attrName, attrValue)
        itemsToUpdate.forEach {
            itemRecDao.updateById(item, it.id as String, itemRec)
        }

        return itemsToUpdate.size
    }

    private fun deleteById(item: Item, id: String): Int =
        if (item.versioned)
            itemRecDao.deleteVersionedById(item, id)
        else
            itemRecDao.deleteById(item, id)

    private fun deleteByAttribute(item: Item, attrName: String, attrValue: Any): Int {
        val itemsToDelete = aclItemRecDao.findAllByAttributeForDelete(item, attrName, attrValue)
        itemsToDelete.forEach { deleteById(item, it.id as String) }

        return itemsToDelete.size
    }

    private fun processCollectionRelations(item: Item, itemRecId: String, strategy: DeletingStrategy) {
        logger.debug("Processing collection relations")
        val collectionRelAttributes = item.spec.attributes.filterValues { it.isCollection() }
        collectionRelAttributes.forEach { (attrName, attribute) ->
            processCollectionRelation(item, itemRecId, attrName, attribute, strategy)
        }
    }

    private fun processCollectionRelation(item: Item, itemRecId: String, relAttrName: String, relAttribute: Attribute, strategy: DeletingStrategy) {
        if (!relAttribute.isCollection())
            throw IllegalArgumentException("Attribute [$relAttrName] is not collection")

        when (val relation = relationManager.getAttributeRelation(item, relAttrName, relAttribute)) {
            is OneToManyInversedBidirectionalRelation -> {
                if (strategy == DeletingStrategy.NO_ACTION)
                    return

                when (strategy) {
                    DeletingStrategy.SET_NULL -> {
                        if (relation.getOwningAttribute().required)
                            throw IllegalStateException("The [${relation.owningAttrName}] is required in item [${relation.owningItem.name}], so it cannot be cleared.")

                        val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to null))
                        updateByAttribute(relation.owningItem, relation.owningAttrName, itemRecId, owningItemRec)
                    }
                    DeletingStrategy.CASCADE -> {
                        // Recursive calls
                        logger.debug("Processing relations recursively")
                        val targetItem = itemService.getByName(requireNotNull(relAttribute.target))
                        val targetItemRecList = aclItemRecDao.findAllByAttributeForDelete(targetItem, relation.owningAttrName, itemRecId)
                        targetItemRecList.forEach { processRelations(targetItem, it, strategy) }

                        deleteByAttribute(relation.owningItem, relation.owningAttrName, itemRecId)
                    }
                    else -> {}
                }
            }
            is ManyToManyRelation -> {
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        deleteByAttribute(relation.intermediateItem, INTERMEDIATE_SOURCE_ATTR_NAME, itemRecId)
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning)
                            deleteByAttribute(relation.intermediateItem, INTERMEDIATE_SOURCE_ATTR_NAME, itemRecId)
                        else
                            deleteByAttribute(relation.intermediateItem, INTERMEDIATE_TARGET_ATTR_NAME, itemRecId)
                    }
                }
            }
            else -> {}
        }
    }

    companion object {
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"

        private val logger = LoggerFactory.getLogger(DeleteRelationHelper::class.java)
    }
}