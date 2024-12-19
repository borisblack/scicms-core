package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.ReloadSchemaIndicator
import org.redisson.api.RAtomicLong
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component

@Component
class ReloadIndicator(redissonClient: RedissonClient) : ReloadSchemaIndicator {
    private var indicator: RAtomicLong = redissonClient.getAtomicLong(RELOAD_INDICATOR_NAME)
    private var lastReloadLocal: Long = System.currentTimeMillis()

    init {
        indicator.set(lastReloadLocal) // always update on start
    }

    fun setNeedReload(isNeedReload: Boolean) {
        if (isNeedReload) {
            indicator.set(System.currentTimeMillis())
        }
    }

    override fun reloadSchema(): Boolean {
        val lastReload = indicator.get()
        if (lastReload > lastReloadLocal) {
            lastReloadLocal = lastReload
            return true
        }

        return false
    }

    companion object {
        private const val RELOAD_INDICATOR_NAME = "scicms_last_reload"
    }
}
