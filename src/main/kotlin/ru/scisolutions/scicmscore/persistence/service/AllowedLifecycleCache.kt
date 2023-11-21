package ru.scisolutions.scicmscore.persistence.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.AllowedLifecycle
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class AllowedLifecycleCache(
    dataProps: DataProps,
    private val allowedLifecycleService: AllowedLifecycleService
) {
    private val cache: Cache<String, List<AllowedLifecycle>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    operator fun get(itemName: String): List<AllowedLifecycle> = cache.get(itemName) {
        allowedLifecycleService.findAllByItemName(itemName)
    }
}