package ru.scisolutions.scicmscore.engine.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.LockHandler
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.hook.LockHook
import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse
import ru.scisolutions.scicmscore.engine.service.ClassService
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import ru.scisolutions.scicmscore.persistence.service.UserCache

@Service
class LockHandlerImpl(
    private val classService: ClassService,
    private val itemCache: ItemCache,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao,
    private val userCache: UserCache
) : LockHandler {
    override fun lock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse {
        val item = itemCache.getOrThrow(itemName)
        if (item.notLockable)
            throw IllegalArgumentException("Item [$itemName] is not lockable")

        val itemRec = aclItemRecDao.findByIdForWrite(item, id) ?: throw IllegalArgumentException("Item [$itemName] with ID [$id] not found")
        if ((itemName == Item.ITEM_TEMPLATE_ITEM_NAME || itemName == Item.ITEM_ITEM_NAME) && itemRec[ItemRec.CORE_ATTR_NAME] == true)
            throw IllegalArgumentException("Item [$itemName] cannot be locked.")

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, LockHook::class.java)
        implInstance?.beforeLock(itemName, id, itemRec)

        val success = itemRecDao.lockById(item, id)
        itemRec.lockedBy = userCache.getCurrent().id

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        val response = FlaggedResponse(
            success = success,
            data = ItemRec(selectData)
        )
        implInstance?.afterLock(itemName, response)

        return response
    }

    override fun unlock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse {
        val item = itemCache.getOrThrow(itemName)
        if (item.notLockable)
            throw IllegalArgumentException("Item [$itemName] is not lockable")

        val itemRec = aclItemRecDao.findByIdForWrite(item, id) ?: throw IllegalArgumentException("Item [$itemName] with ID [$id] not found")
        if ((itemName == Item.ITEM_TEMPLATE_ITEM_NAME || itemName == Item.ITEM_ITEM_NAME) && itemRec[ItemRec.CORE_ATTR_NAME] == true)
            throw IllegalArgumentException("Item [$itemName] cannot be unlocked.")

        if (!aclItemRecDao.existsByIdForWrite(item, id))
            throw AccessDeniedException("You are not allowed to unlock item [$itemName] with ID [$id]")

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, LockHook::class.java)
        implInstance?.beforeUnlock(itemName, id, itemRec)

        val success = itemRecDao.unlockById(item, id)
        itemRec.lockedBy = null

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        val response = FlaggedResponse(
            success = success,
            data = ItemRec(selectData)
        )

        implInstance?.afterUnlock(itemName, response)

        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LockHandlerImpl::class.java)
    }
}