package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.LockHook
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.persistence.service.UserService
import ru.scisolutions.scicmscore.service.ClassService

@Service
class LockHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao,
    private val userService: UserService,
    private val attributeValueHelper: AttributeValueHelper,
) {
    fun lock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse {
        val item = itemService.getByName(itemName)
        if (item.notLockable) {
            throw IllegalArgumentException("Item [$itemName] is not lockable")
        }

        val itemRec = aclItemRecDao.findByIdForWrite(item, id) ?: throw IllegalArgumentException("Item [$itemName] with ID [$id] not found")
        if ((itemName == Item.ITEM_TEMPLATE_ITEM_NAME || itemName == Item.ITEM_ITEM_NAME) && itemRec[ItemRec.CORE_ATTR_NAME] == true) {
            throw IllegalArgumentException("Item [$itemName] cannot be locked.")
        }

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, LockHook::class.java)
        implInstance?.beforeLock(itemName, id, itemRec)

        val success = itemRecDao.lockById(item, id)
        itemRec.lockedBy = userService.getCurrent().id

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        val response =
            FlaggedResponse(
                success = success,
                data = ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData)),
            )
        implInstance?.afterLock(itemName, response)

        return response
    }

    fun unlock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse {
        val item = itemService.getByName(itemName)
        if (item.notLockable) {
            throw IllegalArgumentException("Item [$itemName] is not lockable")
        }

        val itemRec = aclItemRecDao.findByIdForWrite(item, id) ?: throw IllegalArgumentException("Item [$itemName] with ID [$id] not found")
        if ((itemName == Item.ITEM_TEMPLATE_ITEM_NAME || itemName == Item.ITEM_ITEM_NAME) && itemRec[ItemRec.CORE_ATTR_NAME] == true) {
            throw IllegalArgumentException("Item [$itemName] cannot be unlocked.")
        }

        if (!aclItemRecDao.existsByIdForWrite(item, id)) {
            throw AccessDeniedException("You are not allowed to unlock item [$itemName] with ID [$id]")
        }

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, LockHook::class.java)
        implInstance?.beforeUnlock(itemName, id, itemRec)

        val success = itemRecDao.unlockById(item, id)
        itemRec.lockedBy = null

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        val response =
            FlaggedResponse(
                success = success,
                data = ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData)),
            )

        implInstance?.afterUnlock(itemName, response)

        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LockHandler::class.java)
    }
}
