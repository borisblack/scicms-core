package ru.scisolutions.scicmscore.engine.data.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.data.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.service.RelationManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.util.Maps

@Component
class AddRelationHelper(
    private val itemService: ItemService,
    private val relationManager: RelationManager,
    private val auditManager: AuditManager,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) {
    fun processRelations(item: Item, itemRecId: String, relAttributes: Map<String, Any>) {
        relAttributes.forEach { (attrName, value) ->
            processRelation(item, itemRecId, attrName, value)
        }
    }

    private fun processRelation(item: Item, itemRecId: String, relAttrName: String, relAttrValue: Any) {
        val attribute = item.spec.getAttributeOrThrow(relAttrName)
        when (val relation = relationManager.getAttributeRelation(item, relAttrName, attribute)) {
            is OneToOneBidirectionalRelation -> {
                if (relation.isOwning) {
                    val inversedItemRec = ItemRec(mutableMapOf(relation.inversedAttrName to itemRecId))
                    updateOrInsertWithDefaults(relation.inversedItem, relAttrValue as String, inversedItemRec)
                } else {
                    val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to itemRecId))
                    updateOrInsertWithDefaults(relation.owningItem, relAttrValue as String, owningItemRec)
                }
            }
            is OneToManyInversedBidirectionalRelation -> {
                val relIds = relAttrValue as Collection<*>
                val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to itemRecId))
                relIds.forEach { updateOrInsertWithDefaults(relation.owningItem, it as String, owningItemRec) }
            }
            is ManyToManyRelation -> {
                val relIds = relAttrValue as Collection<*>
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        relIds.forEach { addManyToManyRelation(relation.intermediateItem, itemRecId, it as String) }
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning)
                            relIds.forEach { addManyToManyRelation(relation.intermediateItem, itemRecId, it as String) }
                        else
                            relIds.forEach { addManyToManyRelation(relation.intermediateItem, it as String, itemRecId) }
                    }
                }
            }
        }
    }

    private fun updateOrInsertWithDefaults(item: Item, id: String, itemRec: ItemRec): Int {
        if (item.versioned) {
            if (itemService.findByNameForCreate(item.name) == null) {
                logger.warn("Create operation disabled for item [${item.name}].")
                return 0
            }

            val prevItemRec = itemRecDao.findByIdOrThrow(item, id)
            val mergedItemRec = ItemRec(Maps.merge(itemRec, prevItemRec).toMutableMap())
            return itemRecDao.insertWithDefaults(item, mergedItemRec)
        } else {
            if (!aclItemRecDao.existsByIdForWrite(item, id)) {
                logger.warn("Update operation disabled for item [${item.name}] with ID [$id].")
                return 0
            }

            auditManager.assignUpdateAttributes(itemRec)
            return itemRecDao.updateById(item, id, itemRec)
        }
    }

    private fun addManyToManyRelation(intermediateItem: Item, sourceId: String, targetId: String) {
        if (itemService.findByNameForCreate(intermediateItem.name) == null) {
            logger.warn("Create operation disabled for item [${intermediateItem.name}].")
            return
        }

        val intermediateItemRec = ItemRec(mutableMapOf(
            INTERMEDIATE_SOURCE_ATTR_NAME to sourceId,
            INTERMEDIATE_TARGET_ATTR_NAME to targetId
        ))

        itemRecDao.insertWithDefaults(intermediateItem, intermediateItemRec)
    }

    companion object {
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"

        private val logger = LoggerFactory.getLogger(AddRelationHelper::class.java)
    }
}