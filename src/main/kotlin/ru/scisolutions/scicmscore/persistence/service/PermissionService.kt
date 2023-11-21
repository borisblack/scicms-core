package ru.scisolutions.scicmscore.persistence.service

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Access
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.repository.AccessRepository
import ru.scisolutions.scicmscore.persistence.repository.PermissionRepository

@Service
@Repository
@Transactional
class PermissionService(
    private val accessRepository: AccessRepository,
    private val permissionRepository: PermissionRepository,
) {
    @Transactional(readOnly = true)
    fun findById(id: String): Permission? = permissionRepository.findById(id).orElse(null)

    fun getDefault(): Permission = findById(Permission.DEFAULT_PERMISSION_ID)
        ?: throw IllegalArgumentException("Permission [${Permission.DEFAULT_PERMISSION_ID}] not found")

    @Transactional(readOnly = true)
    fun findIdsByMask(mask: Set<Int>): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        val accessList = accessRepository
            .findAllByMask(mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
            .sortedWith(Access.AccessComparator())

        val permissionAccess = accessList.groupBy { it.sourceId }

        return permissionAccess.filterValues { it[0].granting }.keys.toSet()
    }
}