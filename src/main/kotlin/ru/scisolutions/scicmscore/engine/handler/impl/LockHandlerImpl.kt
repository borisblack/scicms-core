package ru.scisolutions.scicmscore.engine.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.LockHandler
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.LockHook
import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse
import ru.scisolutions.scicmscore.engine.service.ClassService
import ru.scisolutions.scicmscore.persistence.service.ItemService

@Service
class LockHandlerImpl(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) : LockHandler {
    override fun lock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse {
        val item = itemService.getByName(itemName)
        if (item.notLockable)
            throw IllegalArgumentException("Item [$itemName] is not lockable")

        if (!aclItemRecDao.existsByIdForWrite(item, id))
            throw IllegalArgumentException("Item [$itemName] with ID [$id] not found")

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, LockHook::class.java)
        implInstance?.beforeLock(itemName, id)

        val success = itemRecDao.lockById(item, id)

        val itemRec = aclItemRecDao.findByIdForWrite(item, id) as ItemRec
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
        val item = itemService.getByName(itemName)
        if (item.notLockable)
            throw IllegalArgumentException("Item [$itemName] is not lockable")

        if (!itemRecDao.existsById(item, id))
            throw IllegalArgumentException("Item [$itemName] with ID [$id] not found")

        if (!aclItemRecDao.existsByIdForWrite(item, id))
            throw AccessDeniedException("You are not allowed to unlock item [$itemName] with ID [$id]")

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, LockHook::class.java)
        implInstance?.beforeUnlock(itemName, id)

        val success = itemRecDao.unlockById(item, id)

        val itemRec = aclItemRecDao.findByIdForWrite(item, id) as ItemRec
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