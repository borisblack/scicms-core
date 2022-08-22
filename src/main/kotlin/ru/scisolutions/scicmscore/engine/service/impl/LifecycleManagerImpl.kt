package ru.scisolutions.scicmscore.engine.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.service.AllowedLifecycleService
import ru.scisolutions.scicmscore.persistence.service.LifecycleService

@Service
class LifecycleManagerImpl(
    private val allowedLifecycleService: AllowedLifecycleService,
    private val lifecycleService: LifecycleService
) : LifecycleManager {
    override fun assignLifecycleAttributes(item: Item, itemRec: ItemRec) {
        val lifecycleId = itemRec.lifecycle
        val allowedLifecycleIds = allowedLifecycleService.findLifecycleIdsByItemName(item.name)
        if (lifecycleId == null) {
            itemRec.lifecycle = if (allowedLifecycleIds.isEmpty()) Lifecycle.DEFAULT_LIFECYCLE_ID else allowedLifecycleIds[0]
        } else {
            if (lifecycleId !in allowedLifecycleIds || lifecycleId != Lifecycle.DEFAULT_LIFECYCLE_ID)
                throw IllegalArgumentException("Lifecycle [$lifecycleId] is not allowed for item [${item.name}]")

            val lifecycle = lifecycleService.getById(lifecycleId)
            val state = itemRec.state
            if (state == null) {
                itemRec.state = lifecycle.startState
            } else {
                if (state !in lifecycle.spec.states)
                    throw IllegalArgumentException("Lifecycle [${lifecycle.name}] doesn't contain state [$state]")
            }
        }
    }
}