package ru.scisolutions.scicmscore.persistence.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.util.Acl.Mask
import java.util.concurrent.TimeUnit

@Service
class PermissionCache(
    dataProps: DataProps,
    private val permissionService: PermissionService
) {
    private val cache: Cache<String, Set<String>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    fun idsForRead(): Set<String> = idsByAccessMask(Mask.READ)

    fun idsForWrite(): Set<String> = idsByAccessMask(Mask.WRITE)

    fun idsForCreate(): Set<String> = idsByAccessMask(Mask.CREATE)

    fun idsForDelete(): Set<String> = idsByAccessMask(Mask.DELETE)

    fun idsForAdministration(): Set<String> = idsByAccessMask(Mask.ADMINISTRATION)

    fun idsByAccessMask(accessMask: Mask): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        return cache.get("${authentication.name}#${accessMask.name}") {
            permissionService.findIdsByMask(accessMask.mask)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PermissionCache::class.java)
    }
}