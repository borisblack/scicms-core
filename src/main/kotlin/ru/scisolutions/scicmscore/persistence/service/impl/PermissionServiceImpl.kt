package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.repository.PermissionRepository
import ru.scisolutions.scicmscore.persistence.service.PermissionService
import ru.scisolutions.scicmscore.util.Acl.Mask
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class PermissionServiceImpl(
    dataProps: DataProps,
    private val permissionRepository: PermissionRepository
) : PermissionService {
    private val permissionIdsCache: Cache<String, Set<String>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun getDefaultPermission(): Permission = permissionRepository.getById(Permission.DEFAULT_PERMISSION_ID)

    @Transactional(readOnly = true)
    override fun findIdsForRead(): Set<String> = findIdsFor(Mask.READ)

    @Transactional(readOnly = true)
    override fun findIdsForWrite(): Set<String> = findIdsFor(Mask.WRITE)

    @Transactional(readOnly = true)
    override fun findIdsForCreate(): Set<String> = findIdsFor(Mask.CREATE)

    @Transactional(readOnly = true)
    override fun findIdsForDelete(): Set<String> = findIdsFor(Mask.DELETE)

    @Transactional(readOnly = true)
    override fun findIdsForAdministration(): Set<String> = findIdsFor(Mask.ADMINISTRATION)

    @Transactional(readOnly = true)
    override fun findIdsFor(accessMask: Mask): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication ?: return emptySet()
        return permissionIdsCache.get("${authentication.name}#${accessMask.name}") {
            permissionRepository.findIdsFor(accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
        }
    }

    @Transactional(readOnly = true)
    override fun findAllForRead(): List<Permission> = findAllFor(Mask.READ)

    @Transactional(readOnly = true)
    override fun findAllForWrite(): List<Permission> = findAllFor(Mask.WRITE)

    @Transactional(readOnly = true)
    override fun findAllForCreate(): List<Permission> = findAllFor(Mask.CREATE)

    @Transactional(readOnly = true)
    override fun findAllForDelete(): List<Permission> = findAllFor(Mask.DELETE)

    @Transactional(readOnly = true)
    override fun findAllForAdministration(): List<Permission> = findAllFor(Mask.ADMINISTRATION)

    private fun findAllFor(accessMask: Mask): List<Permission> {
        val authentication = SecurityContextHolder.getContext().authentication ?: return emptyList()
        return permissionRepository.findAllFor(accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
    }
}