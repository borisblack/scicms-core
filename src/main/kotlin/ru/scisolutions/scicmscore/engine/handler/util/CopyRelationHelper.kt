package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.service.RelationManager

@Component
class CopyRelationHelper(
    private val itemService: ItemService,
    private val relationManager: RelationManager,
    private val itemRecDao: ItemRecDao
) {
    fun copyCollectionRelations(item: Item, prevItemRec: ItemRec, itemRec: ItemRec) {
        logger.debug("Processing collection relations")
        val collectionRelAttributes = item.spec.attributes.filterValues { it.isCollection() }
        collectionRelAttributes.forEach { (attrName, attribute) ->
            copyCollectionRelation(item, prevItemRec, itemRec, attrName, attribute)
        }
    }

    private fun copyCollectionRelation(item: Item, prevItemRec: ItemRec, itemRec: ItemRec, relAttrName: String, relAttribute: Attribute) {
        if (!relAttribute.isCollection()) {
            throw IllegalArgumentException("Attribute [$relAttrName] is not collection")
        }

        when (val relation = relationManager.getAttributeRelation(item, relAttrName, relAttribute)) {
            is OneToManyInversedBidirectionalRelation -> {
                // TODO: Use pagination to avoid OOM error
                val referencedAttrName = relation.getOwningAttribute().referencedBy ?: item.idAttribute
                val prevItemRecId = prevItemRec.getString(referencedAttrName)
                val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.owningItem, relation.owningAttrName, prevItemRecId)
                prevRelItemRecs.forEach {
                    it[relation.owningAttrName] = itemRec.getString(referencedAttrName)
                    insertWithDefaults(relation.owningItem, it)
                }
            }
            is ManyToManyRelation -> {
                val sourceReferencedAttrName = relation.getIntermediateSourceAttribute().referencedBy ?: item.idAttribute
                val sourceItemRecId = itemRec.getString(sourceReferencedAttrName)
                val sourcePrevItemRecId = prevItemRec.getString(sourceReferencedAttrName)
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        // TODO: Use pagination to avoid OOM error
                        val prevRelItemRecs =
                            itemRecDao.findAllByAttribute(
                                relation.intermediateItem,
                                INTERMEDIATE_SOURCE_ATTR_NAME,
                                sourcePrevItemRecId
                            )
                        prevRelItemRecs.forEach {
                            it[INTERMEDIATE_SOURCE_ATTR_NAME] = sourceItemRecId
                            insertWithDefaults(relation.intermediateItem, it)
                        }
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning) {
                            // TODO: Use pagination to avoid OOM error
                            val prevRelItemRecs =
                                itemRecDao.findAllByAttribute(
                                    relation.intermediateItem,
                                    INTERMEDIATE_SOURCE_ATTR_NAME,
                                    sourcePrevItemRecId
                                )
                            prevRelItemRecs.forEach {
                                it[INTERMEDIATE_SOURCE_ATTR_NAME] = sourceItemRecId
                                insertWithDefaults(relation.intermediateItem, it)
                            }
                        } else {
                            // TODO: Use pagination to avoid OOM error
                            val targetReferencedAttrName = relation.getIntermediateTargetAttribute().referencedBy ?: item.idAttribute
                            val targetItemRecId = itemRec.getString(targetReferencedAttrName)
                            val targetPrevItemRecId = prevItemRec.getString(targetReferencedAttrName)
                            val prevRelItemRecs =
                                itemRecDao.findAllByAttribute(
                                    relation.intermediateItem,
                                    INTERMEDIATE_TARGET_ATTR_NAME,
                                    targetPrevItemRecId
                                )
                            prevRelItemRecs.forEach {
                                it[INTERMEDIATE_TARGET_ATTR_NAME] = targetItemRecId
                                insertWithDefaults(relation.intermediateItem, it)
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }

    private fun insertWithDefaults(item: Item, itemRec: ItemRec): Int {
        if (!itemService.canCreate(item.name)) {
            logger.warn("Create operation disabled for item [${item.name}].")
            return 0
        }

        return itemRecDao.insertWithDefaults(item, itemRec)
    }

    companion object {
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"

        private val logger = LoggerFactory.getLogger(CopyRelationHelper::class.java)
    }
}
