package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.RelationManager
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.util.Maps

@Component
class AddRelationHelper(
    private val itemService: ItemService,
    private val relationManager: RelationManager,
    private val auditManager: AuditManager,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) {
    fun addRelations(item: Item, itemRec: ItemRec, relAttributes: Map<String, Any>) {
        relAttributes.forEach { (attrName, value) ->
            addRelation(item, itemRec, attrName, value)
        }
    }

    private fun addRelation(item: Item, itemRec: ItemRec, relAttrName: String, relAttrValue: Any) {
        val attribute = item.spec.getAttribute(relAttrName)
        when (val relation = relationManager.getAttributeRelation(item, relAttrName, attribute)) {
            is OneToOneBidirectionalRelation -> {
                if (!relation.isOwning) {
                    val referencedAttrName = relation.getOwningAttribute().referencedBy ?: relation.owningItem.idAttribute
                    val itemRecId = itemRec.getString(referencedAttrName)
                    val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to itemRecId))
                    updateOrInsertWithDefaults(relation.owningItem, relAttrValue as String, owningItemRec)
                }
            }
            is OneToManyInversedBidirectionalRelation -> {
                val relIds = relAttrValue as Collection<*>
                val referencedAttrName = relation.getOwningAttribute().referencedBy ?: item.idAttribute
                val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to itemRec.getString(referencedAttrName)))
                relIds.forEach { updateOrInsertWithDefaults(relation.owningItem, it as String, owningItemRec) }
            }
            is ManyToManyRelation -> {
                val sourceReferencedAttrName = relation.getIntermediateSourceAttribute().referencedBy ?: item.idAttribute
                val sourceItemRecId = itemRec.getString(sourceReferencedAttrName)
                val relIds = relAttrValue as Collection<*>
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        relIds.forEach { addManyToManyRelation(relation.intermediateItem, sourceItemRecId, it as String) }
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning) {
                            relIds.forEach { addManyToManyRelation(relation.intermediateItem, sourceItemRecId, it as String) }
                        } else {
                            val targetReferencedAttrName = relation.getIntermediateTargetAttribute().referencedBy ?: item.idAttribute
                            val targetItemRecId = itemRec.getString(targetReferencedAttrName)
                            relIds.forEach { addManyToManyRelation(relation.intermediateItem, it as String, targetItemRecId) }
                        }
                    }
                }
            }
        }
    }

    private fun updateOrInsertWithDefaults(item: Item, id: String, itemRec: ItemRec): Int {
        if (item.versioned) {
            if (!itemService.canCreate(item.name)) {
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
        if (!itemService.canCreate(intermediateItem.name)) {
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