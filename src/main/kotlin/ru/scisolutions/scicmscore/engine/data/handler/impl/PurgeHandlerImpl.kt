package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.PurgeHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.handler.util.DeleteRelationHelper
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection
import ru.scisolutions.scicmscore.service.ItemService

@Service
class PurgeHandlerImpl(
    private val itemService: ItemService,
    private val deleteRelationHelper: DeleteRelationHelper,
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

        val itemRecsToPurge = itemRecDao.findAllByAttribute(item, CONFIG_ID_ATTR_NAME, itemRec.configId as String)
        logger.info("${itemRecsToPurge.size} item(s) will be purged")

        // Process relations
        itemRecsToPurge.forEach {
            deleteRelationHelper.processRelations(item, it, input.deletingStrategy)
        }

        itemRecDao.deleteByAttribute(item, CONFIG_ID_ATTR_NAME, itemRec.configId as String) // purge
        logger.info("${itemRecsToPurge.size} item(s) purged.")

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val result = itemRecsToPurge
            .map {
                val selectData = it.filterKeys { key -> key in attrNames }
                ItemRec(selectData.toMutableMap())
            }

        return ResponseCollection(
            data = result
        )
    }

    companion object {
        private const val CONFIG_ID_ATTR_NAME = "configId"

        private val logger = LoggerFactory.getLogger(PurgeHandlerImpl::class.java)
    }
}