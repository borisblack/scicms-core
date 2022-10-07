package ru.scisolutions.scicmscore.engine.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.service.AllowedLifecycleCache
import ru.scisolutions.scicmscore.persistence.service.LifecycleCache

@Service
class LifecycleManagerImpl(
    private val allowedLifecycleCache: AllowedLifecycleCache,
    private val lifecycleCache: LifecycleCache
) : LifecycleManager {
    override fun assignLifecycleAttributes(item: Item, itemRec: ItemRec) {
        var lifecycleId = itemRec.lifecycle
        val allowedLifecycles = allowedLifecycleCache[item.name]
        if (lifecycleId == null) {
            lifecycleId = allowedLifecycles.find { it.isDefault }?.targetId ?: Lifecycle.DEFAULT_LIFECYCLE_ID
            itemRec.lifecycle = lifecycleId
        } else {
            val allowedLifecycleIds = allowedLifecycles.asSequence().map { it.targetId }.toSet() + Lifecycle.DEFAULT_LIFECYCLE_ID
            if (lifecycleId !in allowedLifecycleIds)
                throw IllegalArgumentException("Lifecycle [$lifecycleId] is not allowed for item [${item.name}]")
        }

        val lifecycle = lifecycleCache.getOrThrow(lifecycleId)
        val state = itemRec.state
        if (state == null) {
            itemRec.state = lifecycle.startState
        } else {
            if (state !in lifecycle.parseSpec().states)
                throw IllegalArgumentException("Lifecycle [${lifecycle.name}] doesn't contain state [$state]")
        }
    }
}