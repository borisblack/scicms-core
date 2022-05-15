package ru.scisolutions.scicmscore.engine.data.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.LifecycleManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.service.AllowedLifecycleService
import ru.scisolutions.scicmscore.service.LifecycleService

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

            itemRec.lifecycle = lifecycleId
            itemRec.state = lifecycle.startState
        }
    }
}