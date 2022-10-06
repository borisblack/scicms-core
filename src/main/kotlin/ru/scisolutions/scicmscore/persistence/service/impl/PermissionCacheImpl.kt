package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
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

    override fun getDefault(): Permission = permissionService.findById(Permission.DEFAULT_PERMISSION_ID)
        ?: throw IllegalArgumentException("Permission [${Permission.DEFAULT_PERMISSION_ID}] not found")

    override fun findIdsForRead(): Set<String> = findIdsFor(Mask.READ)

    override fun findIdsForWrite(): Set<String> = findIdsFor(Mask.WRITE)

    override fun findIdsForCreate(): Set<String> = findIdsFor(Mask.CREATE)

    override fun findIdsForDelete(): Set<String> = findIdsFor(Mask.DELETE)

    override fun findIdsForAdministration(): Set<String> = findIdsFor(Mask.ADMINISTRATION)

    override fun findIdsFor(accessMask: Mask): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication ?: return emptySet()
        return cache.get("${authentication.name}#${accessMask.name}") {
            permissionService.findIdsFor(accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
        }
    }
}