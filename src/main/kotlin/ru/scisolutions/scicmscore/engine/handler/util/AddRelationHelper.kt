package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.RelationManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.util.Maps
import java.util.UUID

@Component
class AddRelationHelper(
    private val itemService: ItemService,
    private val relationManager: RelationManager,
    private val auditManager: AuditManager,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) {
    fun processRelations(item: Item, itemRecId: UUID, relAttributes: Map<String, Any>) {
        relAttributes.forEach { (attrName, value) ->
            processRelation(item, itemRecId, attrName, value)
        }
    }

    private fun processRelation(item: Item, itemRecId: UUID, relAttrName: String, relAttrValue: Any) {
        val attribute = item.spec.getAttributeOrThrow(relAttrName)
        when (val relation = relationManager.getAttributeRelation(item, relAttrName, attribute)) {
            is OneToOneBidirectionalRelation -> {
                if (relation.isOwning) {
                    val inversedItemRec = ItemRec(mutableMapOf(relation.inversedAttrName to itemRecId))
                    updateOrInsertWithDefaults(relation.inversedItem, relAttrValue as UUID, inversedItemRec)
                } else {
                    val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to itemRecId))
                    updateOrInsertWithDefaults(relation.owningItem, relAttrValue as UUID, owningItemRec)
                }
            }
            is OneToManyInversedBidirectionalRelation -> {
                val relIds = relAttrValue as Collection<*>
                val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to itemRecId))
                relIds.forEach { updateOrInsertWithDefaults(relation.owningItem, it as UUID, owningItemRec) }
            }
            is ManyToManyRelation -> {
                val relIds = relAttrValue as Collection<*>
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        relIds.forEach { addManyToManyRelation(relation.intermediateItem, itemRecId, it as UUID) }
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning)
                            relIds.forEach { addManyToManyRelation(relation.intermediateItem, itemRecId, it as UUID) }
                        else
                            relIds.forEach { addManyToManyRelation(relation.intermediateItem, it as UUID, itemRecId) }
                    }
                }
            }
        }
    }

    private fun updateOrInsertWithDefaults(item: Item, id: UUID, itemRec: ItemRec): Int {
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

    private fun addManyToManyRelation(intermediateItem: Item, sourceId: UUID, targetId: UUID) {
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