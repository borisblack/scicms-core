package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.handler.util.DeleteMediaHelper
import ru.scisolutions.scicmscore.engine.handler.util.DeleteRelationHelper
import ru.scisolutions.scicmscore.engine.hook.PurgeHook
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection
import ru.scisolutions.scicmscore.service.ClassService
import ru.scisolutions.scicmscore.engine.persistence.service.CacheService
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService

@Service
class PurgeHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val deleteRelationHelper: DeleteRelationHelper,
    private val deleteMediaHelper: DeleteMediaHelper,
    private val attributeValueHelper: AttributeValueHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao,
    private val cacheService: CacheService
) {
    fun purge(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): ResponseCollection {
        val item = itemService.getByName(itemName)
        if (!item.versioned)
            throw IllegalArgumentException("Item [$itemName] is not versioned so it cannot be purged")

        val itemRec = aclItemRecDao.findByIdForDelete(item, input.id)
            ?: throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found.")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        val itemRecsToPurge = itemRecDao.findAllByAttribute(item, CONFIG_ID_ATTR_NAME, itemRec.configId as String)
        logger.info("${itemRecsToPurge.size} item(s) will be purged")

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, PurgeHook::class.java)
        implInstance?.beforePurge(itemName, input, itemRec)

        // Process relations and media
        itemRecsToPurge.forEach {
            deleteRelationHelper.deleteRelations(item, it, input.deletingStrategy)

            // Can be used by another versions or localizations
            if (!item.versioned && !item.localized) {
                deleteMediaHelper.deleteMedia(item, itemRec)
            }
        }

        itemRecDao.deleteByAttribute(item, CONFIG_ID_ATTR_NAME, itemRec.configId as String) // purge
        logger.info("${itemRecsToPurge.size} item(s) purged.")

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val result = itemRecsToPurge
            .map {
                val selectData = it.filterKeys { key -> key in attrNames }
                ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData))
            }

        val response = ResponseCollection(
            data = result
        )

        implInstance?.afterPurge(itemName, response)

        cacheService.optimizeSchemaCaches(item)

        return response
    }

    companion object {
        private const val CONFIG_ID_ATTR_NAME = "configId"

        private val logger = LoggerFactory.getLogger(PurgeHandler::class.java)
    }
}