package ru.scisolutions.scicmscore.engine.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.PurgeHandler
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.handler.util.DeleteLocationHelper
import ru.scisolutions.scicmscore.engine.handler.util.DeleteMediaHelper
import ru.scisolutions.scicmscore.engine.handler.util.DeleteRelationHelper
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.PurgeHook
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection
import ru.scisolutions.scicmscore.engine.service.ClassService
import ru.scisolutions.scicmscore.persistence.service.ItemService

@Service
class PurgeHandlerImpl(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val deleteRelationHelper: DeleteRelationHelper,
    private val deleteMediaHelper: DeleteMediaHelper,
    private val deleteLocationHelper: DeleteLocationHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) : PurgeHandler {
    override fun purge(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): ResponseCollection {
        val item = itemService.getByName(itemName)
        if (!item.versioned)
            throw IllegalArgumentException("Item [$itemName] is not versioned so it cannot be purged")

        val itemRec = aclItemRecDao.findByIdForDelete(item, input.id)
            ?: throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found.")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, PurgeHook::class.java)
        implInstance?.beforePurge(itemName, input)

        val itemRecsToPurge = itemRecDao.findAllByAttribute(item, CONFIG_ID_ATTR_NAME, itemRec.configId as String)
        logger.info("${itemRecsToPurge.size} item(s) will be purged")

        // Process relations, media and locations
        itemRecsToPurge.forEach {
            deleteRelationHelper.processRelations(item, it, input.deletingStrategy)

            // Can be used by another versions or localizations
            if (!item.versioned && !item.localized) {
                deleteMediaHelper.processMedia(item, itemRec)
                deleteLocationHelper.processLocations(item, itemRec)
            }
        }

        itemRecDao.deleteByAttribute(item, CONFIG_ID_ATTR_NAME, itemRec.configId as String) // purge
        logger.info("${itemRecsToPurge.size} item(s) purged.")

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val result = itemRecsToPurge
            .map {
                val selectData = it.filterKeys { key -> key in attrNames }
                ItemRec(selectData.toMutableMap())
            }

        val response = ResponseCollection(
            data = result
        )

        implInstance?.afterPurge(itemName, response)

        return response
    }

    companion object {
        private const val CONFIG_ID_ATTR_NAME = "configId"

        private val logger = LoggerFactory.getLogger(PurgeHandlerImpl::class.java)
    }
}