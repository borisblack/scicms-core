package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.RelationManager
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToManyInversedBidirectionalRelation

@Component
class CopyRelationHelper(
    private val itemService: ItemService,
    private val relationManager: RelationManager,
    private val itemRecDao: ItemRecDao
) {
    fun processCollectionRelations(item: Item, prevItemRecId: String, itemRecId: String) {
        logger.debug("Processing collection relations")
        val collectionRelAttributes = item.spec.attributes.filterValues { it.isCollection() }
        collectionRelAttributes.forEach { (attrName, attribute) ->
            processCollectionRelation(item, prevItemRecId, itemRecId, attrName, attribute)
        }
    }

    private fun processCollectionRelation(item: Item, prevItemRecId: String, itemRecId: String, relAttrName: String, relAttribute: Attribute) {
        if (!relAttribute.isCollection())
            throw IllegalArgumentException("Attribute [$relAttrName] is not collection")

        when (val relation = relationManager.getAttributeRelation(item, relAttrName, relAttribute)) {
            is OneToManyInversedBidirectionalRelation -> {
                // TODO: Use pagination to avoid OOM error
                val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.owningItem, relation.owningAttrName, prevItemRecId)
                prevRelItemRecs.forEach {
                    it[relation.owningAttrName] = itemRecId
                    insertWithDefaults(relation.owningItem, it)
                }
            }
            is ManyToManyRelation -> {
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        // TODO: Use pagination to avoid OOM error
                        val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.intermediateItem, INTERMEDIATE_SOURCE_ATTR_NAME, prevItemRecId)
                        prevRelItemRecs.forEach {
                            it[INTERMEDIATE_SOURCE_ATTR_NAME] = itemRecId
                            insertWithDefaults(relation.intermediateItem, it)
                        }
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning) {
                            // TODO: Use pagination to avoid OOM error
                            val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.intermediateItem, INTERMEDIATE_SOURCE_ATTR_NAME, prevItemRecId)
                            prevRelItemRecs.forEach {
                                it[INTERMEDIATE_SOURCE_ATTR_NAME] = itemRecId
                                insertWithDefaults(relation.intermediateItem, it)
                            }
                        } else {
                            // TODO: Use pagination to avoid OOM error
                            val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.intermediateItem, INTERMEDIATE_TARGET_ATTR_NAME, prevItemRecId)
                            prevRelItemRecs.forEach {
                                it[INTERMEDIATE_TARGET_ATTR_NAME] = itemRecId
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