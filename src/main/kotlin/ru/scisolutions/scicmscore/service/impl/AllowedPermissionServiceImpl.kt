package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.entity.AllowedPermission
import ru.scisolutions.scicmscore.repository.AllowedPermissionRepository
import ru.scisolutions.scicmscore.service.AllowedPermissionService

@Service
@Repository
@Transactional
class AllowedPermissionServiceImpl(private val allowedPermissionRepository: AllowedPermissionRepository) : AllowedPermissionService {
    override fun save(allowedPermission: AllowedPermission): AllowedPermission = allowedPermissionRepository.save(allowedPermission)
}