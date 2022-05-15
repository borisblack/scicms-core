package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.DeleteHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.handler.util.DeleteRelationHelper
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class DeleteHandlerImpl(
    private val itemService: ItemService,
    private val deleteRelationHelper: DeleteRelationHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) : DeleteHandler {
    override fun delete(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)

        if (!itemRecDao.existsById(item, input.id))
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found.")

        val itemRec = aclItemRecDao.findByIdForDelete(item, input.id)
            ?: throw AccessDeniedException("You are not allowed to delete item [$itemName] with ID [${input.id}].")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        deleteRelationHelper.processRelations(item, itemRec, input.deletingStrategy) // process relations

        updateCurrent(item, itemRec)  // if current version

        itemRecDao.deleteById(item, input.id) // delete
        logger.info("Item [$itemName] with ID [${input.id}] deleted.")

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(ItemRec(selectData))
    }

    private fun updateCurrent(item: Item, itemRec: ItemRec) {
        if (!item.versioned || itemRec.current != true)
            return

        logger.debug("Versioned item [${item.name}] with ID ${itemRec.id} is current. Updating group before deleting")
        val itemRecsWithinGroup = itemRecDao.findAllByAttribute(item, CONFIG_ID_ATTR_NAME, itemRec.configId as String)
        val lastItemRec = itemRecsWithinGroup
            .filter { it.id != itemRec.id && it.locale == itemRec.locale }
            .maxByOrNull { it.generation as Int }

        if (lastItemRec != null) {
            logger.debug("Setting current flag for the last versioned item [${item.name}] with ID ${lastItemRec.id}")
            lastItemRec.current = true
            lastItemRec.lastVersion = true
            itemRecDao.updateById(item, lastItemRec.id as String, lastItemRec)
        } else {
            logger.debug("There are no another items [${item.name}] within group")
        }
    }

    companion object {
        private const val CONFIG_ID_ATTR_NAME = "configId"

        private val logger = LoggerFactory.getLogger(DeleteHandlerImpl::class.java)
    }
}