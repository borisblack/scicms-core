package ru.scisolutions.scicmscore.engine.data.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.service.RelationManager
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class CopyRelationHelper(
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
                val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.owningItem, relation.owningAttrName, prevItemRecId)
                prevRelItemRecs.forEach {
                    it[relation.owningAttrName] = itemRecId
                    itemRecDao.insertWithDefaults(relation.owningItem, it)
                }
            }
            is ManyToManyRelation -> {
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.intermediateItem, INTERMEDIATE_SOURCE_ATTR_NAME, prevItemRecId)
                        prevRelItemRecs.forEach {
                            it[INTERMEDIATE_SOURCE_ATTR_NAME] = itemRecId
                            itemRecDao.insertWithDefaults(relation.intermediateItem, it)
                        }
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning) {
                            val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.intermediateItem, INTERMEDIATE_SOURCE_ATTR_NAME, prevItemRecId)
                            prevRelItemRecs.forEach {
                                it[INTERMEDIATE_SOURCE_ATTR_NAME] = itemRecId
                                itemRecDao.insertWithDefaults(relation.intermediateItem, it)
                            }
                        } else {
                            val prevRelItemRecs = itemRecDao.findAllByAttribute(relation.intermediateItem, INTERMEDIATE_TARGET_ATTR_NAME, prevItemRecId)
                            prevRelItemRecs.forEach {
                                it[INTERMEDIATE_TARGET_ATTR_NAME] = itemRecId
                                itemRecDao.insertWithDefaults(relation.intermediateItem, it)
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }

    companion object {
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"

        private val logger = LoggerFactory.getLogger(CopyRelationHelper::class.java)
    }
}