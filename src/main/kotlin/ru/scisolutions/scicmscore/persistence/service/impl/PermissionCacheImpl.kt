package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.Permission
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

    override fun idsForRead(): Set<String> = idsFor(Mask.READ)

    override fun idsForWrite(): Set<String> = idsFor(Mask.WRITE)

    override fun idsForCreate(): Set<String> = idsFor(Mask.CREATE)

    override fun idsForDelete(): Set<String> = idsFor(Mask.DELETE)

    override fun idsForAdministration(): Set<String> = idsFor(Mask.ADMINISTRATION)

    override fun idsFor(accessMask: Mask): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null) {
            logger.warn("Authentication is null, returning empty permission set")
            return emptySet()
        }

        return cache.get("${authentication.name}#${accessMask.name}") {
            permissionService.findIdsFor(accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PermissionCacheImpl::class.java)
    }
}