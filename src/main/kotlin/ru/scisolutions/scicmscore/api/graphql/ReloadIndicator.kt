package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.internal.DefaultDgsQueryExecutor.ReloadSchemaIndicator
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component

@Component
class ReloadIndicator(redissonClient: RedissonClient) : ReloadSchemaIndicator {
    private var indicator = redissonClient.getAtomicLong(RELOAD_INDICATOR_NAME)

    init {
        if (!indicator.isExists)
            indicator.set(0)
    }

    fun setNeedReloadOnce(isNeedReloadOnce: Boolean) {
        indicator.set(if (isNeedReloadOnce) 1 else 0)
    }

    override fun reloadSchema(): Boolean =
        indicator.get() == 1L

    companion object {
        private const val RELOAD_INDICATOR_NAME = "scicms_need_reload"
    }
}