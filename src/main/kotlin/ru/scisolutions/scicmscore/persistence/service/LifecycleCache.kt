package ru.scisolutions.scicmscore.persistence.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import java.util.concurrent.TimeUnit

@Service
class LifecycleCache(
    dataProps: DataProps,
    private val lifecycleService: LifecycleService
) {
    private val cache: Cache<String, Lifecycle> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    operator fun get(id: String): Lifecycle? {
        var lifecycle = cache.getIfPresent(id)
        if (lifecycle == null)
            lifecycle = lifecycleService.findById(id)

        if (lifecycle != null)
            cache.put(id, lifecycle)

        return lifecycle
    }

    fun getOrThrow(id: String): Lifecycle = get(id) ?: throw IllegalArgumentException("Lifecycle [$id] not found")

    fun getDefault(): Lifecycle = getOrThrow(Lifecycle.DEFAULT_LIFECYCLE_ID)
}