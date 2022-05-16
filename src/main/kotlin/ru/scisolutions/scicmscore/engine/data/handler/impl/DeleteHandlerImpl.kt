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
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy
import ru.scisolutions.scicmscore.service.ItemService

@Service
class DeleteHandlerImpl(
    private val itemService: ItemService,
    private val deleteRelationHelper: DeleteRelationHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) : DeleteHandler {
    override fun delete(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): Response {
        if (itemName in disabledItemNames)
            throw IllegalArgumentException("Item [$itemName] cannot be deleted.")

        if (itemName == REVISION_POLICY_ITEM_NAME && input.id == RevisionPolicy.DEFAULT_REVISION_POLICY_ID)
            throw IllegalArgumentException("Default revision policy cannot be deleted.")

        if (itemName == LIFECYCLE_ITEM_NAME && input.id == Lifecycle.DEFAULT_LIFECYCLE_ID)
            throw IllegalArgumentException("Default lifecycle cannot be deleted.")

        if (itemName == PERMISSION_ITEM_NAME && input.id == Permission.DEFAULT_PERMISSION_ID)
            throw IllegalArgumentException("Default permission cannot be deleted.")

        val item = itemService.getByName(itemName)

        if (!itemRecDao.existsById(item, input.id))
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found.")

        val itemRec = aclItemRecDao.findByIdForDelete(item, input.id)
            ?: throw AccessDeniedException("You are not allowed to delete item [$itemName] with ID [${input.id}].")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        deleteRelationHelper.processRelations(item, itemRec, input.deletingStrategy) // process relations

        deleteById(item, input.id) // delete

        logger.info("Item [$itemName] with ID [${input.id}] deleted.")

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(ItemRec(selectData))
    }

    private fun deleteById(item: Item, id: String): Int =
        if (item.versioned)
            itemRecDao.deleteVersionedById(item, id)
        else
            itemRecDao.deleteById(item, id)

    companion object {
        private const val ITEM_ITEM_NAME = "item"
        private const val REVISION_POLICY_ITEM_NAME = "revisionPolicy"
        private const val LIFECYCLE_ITEM_NAME = "lifecycle"
        private const val PERMISSION_ITEM_NAME = "permission"

        private val disabledItemNames = setOf(ITEM_ITEM_NAME)
        private val logger = LoggerFactory.getLogger(DeleteHandlerImpl::class.java)
    }
}