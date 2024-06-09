package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput.DeletingStrategy
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToOneOwningBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.RelationManager

@Component
class DeleteRelationHelper(
    private val itemService: ItemService,
    private val relationManager: RelationManager,
    private val auditManager: AuditManager,
    private val deleteMediaHelper: DeleteMediaHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) {
    fun deleteRelations(item: Item, itemRec: ItemRec, strategy: DeletingStrategy) {
        deleteNonCollectionRelations(item, itemRec, strategy)
        deleteCollectionRelations(item, itemRec, strategy)

        // Remove media. They also can be used by another versions or localizations
        if (!item.versioned && !item.localized) {
            deleteMediaHelper.deleteMedia(item, itemRec)
        }
    }

    private fun deleteNonCollectionRelations(item: Item, itemRec: ItemRec, strategy: DeletingStrategy) {
        if (strategy == DeletingStrategy.NO_ACTION)
            return

        val nonCollectionRelAttributes = item.spec.relationAttributes
            .filterValues { !it.isCollection() }
            .map { (attrName, _) -> attrName to (itemRec[attrName] as String?) }

        nonCollectionRelAttributes.forEach { (attrName, targetId) ->
            deleteNonCollectionRelation(item, itemRec, attrName, targetId, strategy)
        }
    }

    private fun deleteNonCollectionRelation(
        item: Item,
        itemRec: ItemRec,
        relAttrName: String,
        targetId: String?,
        strategy: DeletingStrategy
    ) {
        if (strategy == DeletingStrategy.NO_ACTION)
            return

        val attribute = item.spec.getAttribute(relAttrName)
        when (val relation = relationManager.getAttributeRelation(item, relAttrName, attribute)) {
            is OneToOneBidirectionalRelation -> {
                val referencedAttrName = relation.getOwningAttribute().referencedBy ?: relation.owningItem.idAttribute
                when (strategy) {
                    DeletingStrategy.SET_NULL -> {
                        if (!relation.isOwning) {
                            if (relation.getOwningAttribute().required)
                                throw IllegalStateException("The [${relation.owningAttrName}] is required in item [${relation.owningItem.name}], so it cannot be cleared.")

                            val referencedRecId = itemRec.getString(referencedAttrName)
                            val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to null))
                            updateByAttribute(relation.owningItem, relation.owningAttrName, referencedRecId, owningItemRec)
                        }
                    }
                    DeletingStrategy.CASCADE -> {
                        logger.debug("Deleting oneToOne relations recursively")

                        if (relation.isOwning) {
                            val targetItemRecs = aclItemRecDao.findAllByAttributeForDelete(relation.inversedItem, referencedAttrName, requireNotNull(targetId))

                            // Should be only one
                            for (targetItemRec in targetItemRecs) {
                                deleteRelations(relation.inversedItem, targetItemRec, strategy)
                                deleteByAttribute(relation.inversedItem, referencedAttrName, targetId)
                            }
                        } else if (ENABLE_ONE_TO_ONE_NOT_OWNING_CASCADE_DELETE) {
                            val referencedRecId = itemRec.getString(referencedAttrName)
                            val targetItemRecs = aclItemRecDao.findAllByAttributeForDelete(relation.owningItem, relation.owningAttrName, referencedRecId)

                            // Should be only one
                            for (targetItemRec in targetItemRecs) {
                                deleteRelations(relation.owningItem, targetItemRec, strategy)
                                deleteByAttribute(relation.owningItem, relation.owningAttrName, referencedRecId)
                            }
                        }
                    }
                    else -> {}
                }
            }
            is ManyToOneOwningBidirectionalRelation -> {
                val referencedAttrName = relation.getOwningAttribute().referencedBy ?: relation.owningItem.idAttribute
                when (strategy) {
                    DeletingStrategy.SET_NULL -> {
                        if (relation.getOwningAttribute().required)
                            throw IllegalStateException("The [${relation.owningAttrName}] is required in item [${relation.owningItem.name}], so it cannot be cleared.")

                        val referencedRecId = itemRec.getString(referencedAttrName)
                        val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to null))
                        updateByAttribute(relation.owningItem, relation.owningAttrName, referencedRecId, owningItemRec)
                    }
                    DeletingStrategy.CASCADE -> {
                        if (ENABLE_MANY_TO_ONE_OWNING_CASCADE_DELETE) {
                            logger.debug("Deleting manyToOne relations recursively")

                            val targetItemRecs = aclItemRecDao.findAllByAttributeForDelete(relation.inversedItem, referencedAttrName, requireNotNull(targetId))

                            // Should be only one
                            for (targetItemRec in targetItemRecs) {
                                deleteRelations(relation.inversedItem, targetItemRec, strategy)
                                deleteByAttribute(relation.inversedItem, referencedAttrName, targetId)
                            }
                        }
                    }
                    else -> {}
                }
            }
            else -> {}
        }
    }

    private fun updateByAttribute(item: Item, attrName: String, attrValue: Any, itemRec: ItemRec): Int {
        auditManager.assignUpdateAttributes(itemRec)
        val itemsToUpdate = aclItemRecDao.findAllByAttributeForWrite(item, attrName, attrValue)
        itemsToUpdate.forEach {
            itemRecDao.updateById(item, it.getString(item.idAttribute), itemRec)
        }

        return itemsToUpdate.size
    }

    private fun deleteByAttribute(item: Item, attrName: String, attrValue: Any): Int {
        val itemsToDelete = aclItemRecDao.findAllByAttributeForDelete(item, attrName, attrValue)
        itemsToDelete.forEach { deleteById(item, it.getString(item.idAttribute)) }

        return itemsToDelete.size
    }

    private fun deleteById(item: Item, id: String): Int =
        if (item.versioned)
            itemRecDao.deleteVersionedById(item, id)
        else
            itemRecDao.deleteById(item, id)

    private fun deleteCollectionRelations(item: Item, itemRec: ItemRec, strategy: DeletingStrategy) {
        val collectionRelAttributes = item.spec.relationAttributes.filterValues { it.isCollection() }
        collectionRelAttributes.forEach { (attrName, attribute) ->
            deleteCollectionRelation(item, itemRec, attrName, attribute, strategy)
        }
    }

    private fun deleteCollectionRelation(item: Item, itemRec: ItemRec, relAttrName: String, relAttribute: Attribute, strategy: DeletingStrategy) {
        if (!relAttribute.isCollection())
            throw IllegalArgumentException("Attribute [$relAttrName] is not collection")

        when (val relation = relationManager.getAttributeRelation(item, relAttrName, relAttribute)) {
            is OneToManyInversedBidirectionalRelation -> {
                if (strategy == DeletingStrategy.NO_ACTION)
                    return

                val referencedAttrName = relation.getOwningAttribute().referencedBy ?: item.idAttribute
                val referencedKey = itemRec.getString(referencedAttrName)
                when (strategy) {
                    DeletingStrategy.SET_NULL -> {
                        if (relation.getOwningAttribute().required)
                            throw IllegalStateException("The [${relation.owningAttrName}] is required in item [${relation.owningItem.name}], so it cannot be cleared.")

                        val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to null))
                        updateByAttribute(relation.owningItem, relation.owningAttrName, referencedKey, owningItemRec)
                    }
                    DeletingStrategy.CASCADE -> {
                        // Recursive calls
                        logger.debug("Processing relations recursively")
                        val targetItem = itemService.getByName(requireNotNull(relAttribute.target))
                        val targetItemRecList = aclItemRecDao.findAllByAttributeForDelete(targetItem, relation.owningAttrName, referencedKey)
                        targetItemRecList.forEach { deleteRelations(targetItem, it, strategy) }

                        deleteByAttribute(relation.owningItem, relation.owningAttrName, referencedKey)
                    }
                    else -> {}
                }
            }
            is ManyToManyRelation -> {
                val sourceReferencedAttrName = relation.getIntermediateSourceAttribute().referencedBy ?: item.idAttribute
                val sourceItemRecId = itemRec.getString(sourceReferencedAttrName)
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        deleteByAttribute(relation.intermediateItem, INTERMEDIATE_SOURCE_ATTR_NAME, sourceItemRecId)
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning) {
                            deleteByAttribute(relation.intermediateItem, INTERMEDIATE_SOURCE_ATTR_NAME, sourceItemRecId)
                        } else {
                            val targetReferencedAttrName = relation.getIntermediateTargetAttribute().referencedBy ?: item.idAttribute
                            val targetItemRecId = itemRec.getString(targetReferencedAttrName)
                            deleteByAttribute(relation.intermediateItem, INTERMEDIATE_TARGET_ATTR_NAME, targetItemRecId)
                        }
                    }
                }
            }
            else -> {}
        }
    }

    companion object {
        private const val ENABLE_ONE_TO_ONE_NOT_OWNING_CASCADE_DELETE = false;
        private const val ENABLE_MANY_TO_ONE_OWNING_CASCADE_DELETE = false;
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"

        private val logger = LoggerFactory.getLogger(DeleteRelationHelper::class.java)
    }
}