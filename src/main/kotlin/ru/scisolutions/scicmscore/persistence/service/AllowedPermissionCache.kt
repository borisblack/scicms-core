package ru.scisolutions.scicmscore.persistence.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class AllowedPermissionCache(
    dataProps: DataProps,
    private val allowedPermissionService: AllowedPermissionService
) {
    val cache: Cache<String, List<AllowedPermission>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    operator fun get(itemName: String): List<AllowedPermission> = cache.get(itemName) {
        allowedPermissionService.findAllByItemName(itemName)
    }
}