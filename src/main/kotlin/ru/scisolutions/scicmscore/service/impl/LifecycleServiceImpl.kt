package ru.scisolutions.scicmscore.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.repository.LifecycleRepository
import ru.scisolutions.scicmscore.service.LifecycleService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class LifecycleServiceImpl(
    dataProps: DataProps,
    private val lifecycleRepository: LifecycleRepository
) : LifecycleService {
    private val lifecycleCache: Cache<String, Lifecycle> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun getDefaultLifecycle(): Lifecycle = getById(Lifecycle.DEFAULT_LIFECYCLE_ID)

    @Transactional(readOnly = true)
    override fun getById(id: String): Lifecycle = lifecycleCache.get(id) { lifecycleRepository.getById(id) }
}