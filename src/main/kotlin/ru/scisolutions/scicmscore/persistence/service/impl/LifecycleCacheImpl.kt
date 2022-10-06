package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.service.LifecycleCache
import ru.scisolutions.scicmscore.persistence.service.LifecycleService
import java.util.concurrent.TimeUnit

@Service
class LifecycleCacheImpl(
    dataProps: DataProps,
    private val lifecycleService: LifecycleService
) : LifecycleCache {
    private val cache: Cache<String, Lifecycle> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override operator fun get(id: String): Lifecycle? {
        var lifecycle = cache.getIfPresent(id)
        if (lifecycle == null)
            lifecycle = lifecycleService.findById(id)

        if (lifecycle != null)
            cache.put(id, lifecycle)

        return lifecycle
    }

    override fun getOrThrow(id: String): Lifecycle = get(id) ?: throw IllegalArgumentException("Lifecycle [$id] not found")

    override fun getDefault(): Lifecycle = getOrThrow(Lifecycle.DEFAULT_LIFECYCLE_ID)
}