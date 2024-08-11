package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.lifecycle.Promotable
import ru.scisolutions.scicmscore.engine.model.input.PromoteInput
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.persistence.service.LifecycleService
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.service.ClassService

@Service
class PromoteHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val lifecycleService: LifecycleService,
    private val auditManager: AuditManager,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao,
    private val attributeValueHelper: AttributeValueHelper,
) {
    fun promote(itemName: String, input: PromoteInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)

        val itemRec =
            aclItemRecDao.findByIdForWrite(item, input.id)
                ?: throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found.")

        if (!item.notLockable) {
            itemRecDao.lockByIdOrThrow(item, input.id)
        }

        val lifecycleId =
            itemRec.lifecycle
                ?: throw IllegalStateException("Item [$itemName] with ID [${input.id}] has no lifecycle.")

        val lifecycle = lifecycleService.getById(lifecycleId)
        if (itemRec.state == input.state) {
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] is already in the [${itemRec.state}] state.")
        }

        val spec = lifecycle.parseSpec()
        if (itemRec.state == null) {
            if (input.state !in spec.startEvent.transitions) {
                throw IllegalArgumentException("Transition to the [${input.state}] state is not allowed.")
            }
        } else {
            val currentState = spec.getStateOrThrow(itemRec.state as String)
            if (input.state !in currentState.transitions) {
                throw IllegalArgumentException("Transition to the [${input.state}] state is not allowed.")
            }
        }

        itemRec.state = input.state

        auditManager.assignUpdateAttributes(itemRec)

        itemRecDao.updateById(item, input.id, itemRec) // update

        // Call implementation
        if (!lifecycle.implementation.isNullOrBlank()) {
            logger.debug("Calling lifecycle implementation [${lifecycle.implementation}]")
            val instance = getPromoteInstance(lifecycle)
            instance.promote(itemName, input.id, input.state)
        }

        if (!item.notLockable) {
            itemRecDao.unlockByIdOrThrow(item, input.id)
        }

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(
            ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData)),
        )
    }

    private fun getPromoteInstance(lifecycle: Lifecycle): Promotable {
        val implementation = lifecycle.implementation
        if (implementation.isNullOrBlank()) {
            throw IllegalArgumentException("Lifecycle [${lifecycle.name}] has no implementation.")
        }

        val clazz = Class.forName(implementation)
        return if (Promotable::class.java.isAssignableFrom(clazz)) {
            classService.getInstance(clazz) as Promotable
        } else {
            throw IllegalStateException("Class [${clazz.simpleName}] does not implement [${Promotable::class.simpleName}] interface.")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PromoteHandler::class.java)
    }
}
