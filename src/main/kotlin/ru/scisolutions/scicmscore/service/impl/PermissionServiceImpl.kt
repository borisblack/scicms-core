package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.entity.Permission
import ru.scisolutions.scicmscore.repository.PermissionRepository
import ru.scisolutions.scicmscore.service.PermissionService

@Service
class PermissionServiceImpl(private val permissionRepository: PermissionRepository) : PermissionService {
    override val defaultPermission: Permission by lazy { fetchDefaultPermission() }

    private fun fetchDefaultPermission(): Permission = permissionRepository.getById(Permission.DEFAULT_PERMISSION_ID)
}