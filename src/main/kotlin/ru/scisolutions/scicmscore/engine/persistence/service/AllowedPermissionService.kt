package ru.scisolutions.scicmscore.engine.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.entity.AllowedPermission
import ru.scisolutions.scicmscore.engine.persistence.repository.AllowedPermissionRepository

@Service
@Repository
@Transactional
class AllowedPermissionService(
    private val allowedPermissionRepository: AllowedPermissionRepository,
) {
    @Transactional(readOnly = true)
    fun findAllByItemName(itemName: String): List<AllowedPermission> = allowedPermissionRepository.findAllByItemName(itemName)
}
