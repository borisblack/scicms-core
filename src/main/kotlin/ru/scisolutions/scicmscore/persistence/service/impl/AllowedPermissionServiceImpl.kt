package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission
import ru.scisolutions.scicmscore.persistence.repository.AllowedPermissionRepository
import ru.scisolutions.scicmscore.persistence.service.AllowedPermissionService

@Service
@Repository
@Transactional
class AllowedPermissionServiceImpl(
    private val allowedPermissionRepository: AllowedPermissionRepository
) : AllowedPermissionService {
    @Transactional(readOnly = true)
    override fun findAllByItemName(itemName: String): List<AllowedPermission> =
        allowedPermissionRepository.findAllByItemName(itemName)
}