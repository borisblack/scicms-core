package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.service.PermissionCache
import ru.scisolutions.scicmscore.persistence.service.PermissionService
import ru.scisolutions.scicmscore.util.Acl.Mask
import java.util.concurrent.TimeUnit

@Service
class PermissionCacheImpl(
    dataProps: DataProps,
    private val permissionService: PermissionService
) : PermissionCache {
    private val cache: Cache<String, Set<String>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override fun idsForRead(): Set<String> = idsByAccessMask(Mask.READ)

    override fun idsForWrite(): Set<String> = idsByAccessMask(Mask.WRITE)

    override fun idsForCreate(): Set<String> = idsByAccessMask(Mask.CREATE)

    override fun idsForDelete(): Set<String> = idsByAccessMask(Mask.DELETE)

    override fun idsForAdministration(): Set<String> = idsByAccessMask(Mask.ADMINISTRATION)

    override fun idsByAccessMask(accessMask: Mask): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        return cache.get("${authentication.name}#${accessMask.name}") {
            permissionService.findIdsByMask(accessMask.mask)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PermissionCacheImpl::class.java)
    }
}