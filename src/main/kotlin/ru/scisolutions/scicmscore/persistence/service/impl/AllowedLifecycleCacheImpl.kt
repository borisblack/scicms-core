package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.AllowedLifecycle
import ru.scisolutions.scicmscore.persistence.service.AllowedLifecycleCache
import ru.scisolutions.scicmscore.persistence.service.AllowedLifecycleService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class AllowedLifecycleCacheImpl(
    dataProps: DataProps,
    private val allowedLifecycleService: AllowedLifecycleService
) : AllowedLifecycleCache {
    private val cache: Cache<String, List<AllowedLifecycle>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override fun findAllByItemName(itemName: String): List<AllowedLifecycle> = cache.get(itemName) {
        allowedLifecycleService.findAllByItemName(itemName)
    }
}