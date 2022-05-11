package ru.scisolutions.scicmscore.service.impl

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
import ru.scisolutions.scicmscore.service.PermissionService
import ru.scisolutions.scicmscore.util.ACL.Mask
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class PermissionServiceImpl(
    dataProps: DataProps,
    private val permissionRepository: PermissionRepository
) : PermissionService {
    private val permissionIdsCache: Cache<String, Set<String>> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.permissionIdsCacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override val defaultPermission: Permission by lazy { fetchDefaultPermission() }

    @Transactional(readOnly = true)
    fun fetchDefaultPermission(): Permission = permissionRepository.getById(Permission.DEFAULT_PERMISSION_ID)

    @Transactional(readOnly = true)
    override fun getIdsForRead(): Set<String> = getIdsFor(Mask.READ)

    @Transactional(readOnly = true)
    override fun getIdsForWrite(): Set<String> = getIdsFor(Mask.WRITE)

    @Transactional(readOnly = true)
    override fun getIdsForCreate(): Set<String> = getIdsFor(Mask.CREATE)

    @Transactional(readOnly = true)
    override fun getIdsForDelete(): Set<String> = getIdsFor(Mask.DELETE)

    @Transactional(readOnly = true)
    override fun getIdsForAdministration(): Set<String> = getIdsFor(Mask.ADMINISTRATION)

    @Transactional(readOnly = true)
    override fun getIdsFor(accessMask: Mask): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
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
        val authentication = SecurityContextHolder.getContext().authentication
        return permissionRepository.findAllFor(accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
    }
}