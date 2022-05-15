package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.LockHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.service.ItemService

@Service
class LockHandlerImpl(
    private val itemService: ItemService,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) : LockHandler {
    override fun lock(itemName: String, id: String, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (item.notLockable)
            throw IllegalArgumentException("Item [$itemName] is not lockable")

        if (!itemRecDao.existsById(item, id))
            throw IllegalArgumentException("Item [$itemName] with ID [$id] not found")

        if (!aclItemRecDao.existsByIdForWrite(item, id)) // not locked
            throw AccessDeniedException("You are not allowed to lock item [$itemName] with ID [$id]")

        itemRecDao.lockByIdOrThrow(item, id)

        val itemRec = aclItemRecDao.findByIdForWrite(item, id) as ItemRec
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(ItemRec(selectData))
    }

    override fun unlock(itemName: String, id: String, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (item.notLockable)
            throw IllegalArgumentException("Item [$itemName] is not lockable")

        if (!itemRecDao.existsById(item, id))
            throw IllegalArgumentException("Item [$itemName] with ID [$id] not found")

        if (!aclItemRecDao.existsByIdForWrite(item, id))
            throw AccessDeniedException("You are not allowed to unlock item [$itemName] with ID [$id]")

        itemRecDao.unlockByIdOrThrow(item, id)

        val itemRec = aclItemRecDao.findByIdForWrite(item, id) as ItemRec
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(ItemRec(selectData))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LockHandlerImpl::class.java)
    }
}