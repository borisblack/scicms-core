package ru.scisolutions.scicmscore.engine.data.handler.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.PromoteHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.PromoteInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.schema.model.Promotable
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.service.LifecycleService

@Service
class PromoteHandlerImpl(
    private val applicationContext: ApplicationContext,
    private val itemService: ItemService,
    private val lifecycleService: LifecycleService,
    private val auditManager: AuditManager,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) : PromoteHandler {
    private val classCache: Cache<String, Class<*>> = CacheBuilder.newBuilder().build()
    private val instanceCache: Cache<Class<*>, Any> = CacheBuilder.newBuilder().build()

    override fun promote(itemName: String, input: PromoteInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)

        val itemRec = aclItemRecDao.findByIdForWrite(item, input.id)
            ?: throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found.")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        val lifecycleId = itemRec.lifecycle
            ?: throw IllegalStateException("Item [$itemName] with ID [${input.id}] has no lifecycle.")

        val lifecycle = lifecycleService.getById(lifecycleId)

        val currentStateName = itemRec.state ?: lifecycle.startState
        if (currentStateName == input.state)
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] is already in the [$currentStateName] state.")

        val currentState = lifecycle.spec.getStateOrThrow(currentStateName)
        if (input.state !in currentState.transitions)
            throw IllegalArgumentException("Transition to the [${input.state}] state is not allowed.")

        itemRec.state = input.state

        auditManager.assignUpdateAttributes(itemRec)

        itemRecDao.updateById(item, input.id, itemRec) // update

        // Call implementation
        if (!lifecycle.implementation.isNullOrBlank()) {
            logger.debug("Calling lifecycle implementation [${lifecycle.implementation}]")
            val instance = getPromoteInstance(lifecycle)
            instance.promote(itemName, input.id, input.state)
        }

        if (!item.notLockable)
            itemRecDao.unlockByIdOrThrow(item, input.id)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(ItemRec(selectData))
    }

    private fun getPromoteInstance(lifecycle: Lifecycle): Promotable {
        val implementation = lifecycle.implementation
        if (implementation.isNullOrBlank())
            throw IllegalArgumentException("Lifecycle [${lifecycle.name}] has no implementation.")

        val clazz = getClass(implementation)
        return if (Promotable::class.java.isAssignableFrom(clazz))
            getInstance(clazz) as Promotable
        else
            throw IllegalStateException("Class [${clazz.simpleName}] does not implement [${Promotable::class.simpleName}] interface.")
    }

    private fun getClass(className: String): Class<*> = classCache.get(className) { Class.forName(className) }

    private fun getInstance(clazz: Class<*>): Any = instanceCache.get(clazz) {
        try {
            applicationContext.getBean(clazz)
        } catch (e: BeansException) {
            clazz.getConstructor().newInstance()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PromoteHandlerImpl::class.java)
    }
}