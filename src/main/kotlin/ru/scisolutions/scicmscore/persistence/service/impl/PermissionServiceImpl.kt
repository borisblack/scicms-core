package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.repository.PermissionRepository
import ru.scisolutions.scicmscore.persistence.service.PermissionService
import ru.scisolutions.scicmscore.util.Acl.Mask

@Service
@Repository
@Transactional
class PermissionServiceImpl(
    private val permissionRepository: PermissionRepository
) : PermissionService {
    @Transactional(readOnly = true)
    override fun findById(id: String): Permission? = permissionRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    override fun findIdsFor(mask: Set<Int>, username: String, roles: Set<String>): Set<String> =
        permissionRepository.findIdsFor(mask, username, roles)

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