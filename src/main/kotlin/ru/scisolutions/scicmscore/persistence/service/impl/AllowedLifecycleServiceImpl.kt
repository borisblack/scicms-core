package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.repository.AllowedLifecycleRepository
import ru.scisolutions.scicmscore.persistence.service.AllowedLifecycleService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class AllowedLifecycleServiceImpl(
    dataProps: DataProps,
    private val allowedLifecycleRepository: AllowedLifecycleRepository
) : AllowedLifecycleService {
    private val lifecycleIdsCache: Cache<String, List<String>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun findLifecycleIdsByItemName(itemName: String): List<String> = lifecycleIdsCache.get(itemName) {
        allowedLifecycleRepository.findLifecycleIdsByItemName(itemName)
    }
}