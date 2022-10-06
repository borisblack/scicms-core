package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission
import ru.scisolutions.scicmscore.persistence.service.AllowedPermissionCache
import ru.scisolutions.scicmscore.persistence.service.AllowedPermissionService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class AllowedPermissionCacheImpl(
    dataProps: DataProps,
    private val allowedPermissionService: AllowedPermissionService
) : AllowedPermissionCache {
    private val cache: Cache<String, List<AllowedPermission>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun findAllByItemName(itemName: String): List<AllowedPermission> = cache.get(itemName) {
        allowedPermissionService.findAllByItemName(itemName)
    }
}