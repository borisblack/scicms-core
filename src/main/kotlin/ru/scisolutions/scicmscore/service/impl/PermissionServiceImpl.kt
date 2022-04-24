package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.repository.PermissionRepository
import ru.scisolutions.scicmscore.service.PermissionService

@Service
@Repository
@Transactional
class PermissionServiceImpl(private val permissionRepository: PermissionRepository) : PermissionService {
    override val defaultPermission: Permission by lazy { fetchDefaultPermission() }

    @Transactional(readOnly = true)
    fun fetchDefaultPermission(): Permission = permissionRepository.getById(Permission.DEFAULT_PERMISSION_ID)
}