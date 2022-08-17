package ru.scisolutions.scicmscore.engine.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.AllowedLifecycleService
import ru.scisolutions.scicmscore.persistence.service.LifecycleService

@Service
class LifecycleManagerImpl(
    private val allowedLifecycleService: AllowedLifecycleService,
    private val lifecycleService: LifecycleService
) : LifecycleManager {
    override fun assignLifecycleAttributes(item: Item, itemRec: ItemRec) {
        val lifecycleId = itemRec.lifecycle
        if (lifecycleId == null) {
            // itemRec.lifecycle = Lifecycle.DEFAULT_LIFECYCLE_ID
        } else {
            if (lifecycleId !in allowedLifecycleService.findLifecycleIdsByItemName(item.name))
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