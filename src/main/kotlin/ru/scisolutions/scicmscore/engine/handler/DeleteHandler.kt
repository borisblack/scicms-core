package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.handler.util.DeleteMediaHelper
import ru.scisolutions.scicmscore.engine.handler.util.DeleteRelationHelper
import ru.scisolutions.scicmscore.engine.hook.DeleteHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.service.ClassService
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy
import ru.scisolutions.scicmscore.persistence.service.ItemService

@Service
class DeleteHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val deleteRelationHelper: DeleteRelationHelper,
    private val deleteMediaHelper: DeleteMediaHelper,
    private val attributeValueHelper: AttributeValueHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) {
    fun delete(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): Response {
        if (itemName == Item.REVISION_POLICY_ITEM_NAME && input.id == RevisionPolicy.DEFAULT_REVISION_POLICY_ID)
            throw IllegalArgumentException("Default revision policy cannot be deleted.")

        if (itemName == Item.LIFECYCLE_ITEM_NAME && input.id == Lifecycle.DEFAULT_LIFECYCLE_ID)
            throw IllegalArgumentException("Default lifecycle cannot be deleted.")

        if (itemName == Item.PERMISSION_ITEM_NAME && input.id == Permission.DEFAULT_PERMISSION_ID)
            throw IllegalArgumentException("Default permission cannot be deleted.")

        val item = itemService.getByName(itemName)

        if (!itemRecDao.existsById(item, input.id))
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found.")

        val itemRec = aclItemRecDao.findByIdForDelete(item, input.id)
            ?: throw AccessDeniedException("You are not allowed to delete item [$itemName] with ID [${input.id}].")

        if ((itemName == Item.ITEM_TEMPLATE_ITEM_NAME || itemName == Item.ITEM_ITEM_NAME) && itemRec[ItemRec.CORE_ATTR_NAME] == true)
            throw IllegalArgumentException("Item [$itemName] cannot be deleted.")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, DeleteHook::class.java)
        implInstance?.beforeDelete(itemName, input, itemRec)

        deleteRelationHelper.processRelations(item, itemRec, input.deletingStrategy) // process relations

        // Can be used by another versions or localizations
        if (!item.versioned && !item.localized) {
            deleteMediaHelper.processMedia(item, itemRec)
        }

        deleteById(item, input.id) // delete

        logger.info("Item [$itemName] with ID [${input.id}] deleted.")

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        val response = Response(
            ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData))
        )

        implInstance?.afterDelete(itemName, response)

        return response
    }

    private fun deleteById(item: Item, id: String): Int =
        if (item.versioned)
            itemRecDao.deleteVersionedById(item, id)
        else
            itemRecDao.deleteById(item, id)

    companion object {
        private val logger = LoggerFactory.getLogger(DeleteHandler::class.java)
    }
}