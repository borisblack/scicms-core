package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.repository.PermissionRepository
import ru.scisolutions.scicmscore.persistence.service.PermissionService

@Service
@Repository
@Transactional
class PermissionServiceImpl(
    private val permissionRepository: PermissionRepository
) : PermissionService {
    @Transactional(readOnly = true)
    override fun findById(id: String): Permission? = permissionRepository.findById(id).orElse(null)

    override fun getDefault(): Permission = findById(Permission.DEFAULT_PERMISSION_ID)
        ?: throw IllegalArgumentException("Permission [${Permission.DEFAULT_PERMISSION_ID}] not found")

    @Transactional(readOnly = true)
    override fun findIdsFor(mask: Set<Int>, username: String, roles: Set<String>): Set<String> =
        permissionRepository.findIdsFor(mask, username, roles)
}