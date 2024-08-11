package ru.scisolutions.scicmscore.engine.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AclHelper
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.engine.persistence.service.AllowedLifecycleService
import ru.scisolutions.scicmscore.engine.persistence.service.LifecycleService

@Service
class LifecycleManager(
    private val allowedLifecycleService: AllowedLifecycleService,
    private val lifecycleService: LifecycleService,
    private val aclHelper: AclHelper,
) {
    fun assignLifecycleAttributes(item: Item, itemRec: ItemRec) {
        var lifecycleId = itemRec.lifecycle
        val allowedLifecycles = allowedLifecycleService.findAllByItemName(item.name)
        if (lifecycleId == null) {
            lifecycleId = allowedLifecycles.find { it.isDefault }?.targetId ?: Lifecycle.DEFAULT_LIFECYCLE_ID
            itemRec.lifecycle = lifecycleId
        } else {
            val allowedLifecycleIds = allowedLifecycles.asSequence().map { it.targetId }.toSet() + Lifecycle.DEFAULT_LIFECYCLE_ID
            if (lifecycleId !in allowedLifecycleIds) {
                throw IllegalArgumentException("Lifecycle [$lifecycleId] is not allowed for item [${item.name}]")
            }
        }

        val lifecycle = lifecycleService.getById(lifecycleId)
        val state = itemRec.state
        if (state != null && state !in lifecycle.parseSpec().states) {
            throw IllegalArgumentException("Lifecycle [${lifecycle.name}] doesn't contain state [$state]")
        }
    }

    fun assignLifecycleAttributes(item: Item, prevItemRec: ItemRec, itemRec: ItemRec) {
        var lifecycleId = itemRec.lifecycle
        val allowedLifecycles = allowedLifecycleService.findAllByItemName(item.name)
        val defaultLifecycleId = allowedLifecycles.find { it.isDefault }?.targetId ?: Lifecycle.DEFAULT_LIFECYCLE_ID
        if (lifecycleId == null) {
            lifecycleId = prevItemRec.lifecycle ?: defaultLifecycleId
            itemRec.lifecycle = lifecycleId
        } else {
            if (!aclHelper.canAdmin(prevItemRec)) {
                logger.warn("User cannot change lifecycle")
                lifecycleId = prevItemRec.lifecycle ?: defaultLifecycleId
            }

            val allowedLifecycleIds = allowedLifecycles.asSequence().map { it.targetId }.toSet() + Lifecycle.DEFAULT_LIFECYCLE_ID
            if (lifecycleId !in allowedLifecycleIds) {
                logger.warn(
                    "Lifecycle '$lifecycleId' is not allowed for item '${item.name}'. Resetting to default lifecycle '$defaultLifecycleId'",
                )
                lifecycleId = defaultLifecycleId
            }
        }

        val lifecycle = lifecycleService.getById(lifecycleId)
        val state = itemRec.state
        if (state != null && state !in lifecycle.parseSpec().states) {
            throw IllegalArgumentException("Lifecycle [${lifecycle.name}] doesn't contain state [$state]")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LifecycleManager::class.java)
    }
}
