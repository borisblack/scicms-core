package ru.scisolutions.scicmscore.engine.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.service.AllowedPermissionService

@Service
class PermissionManagerImpl(private val allowedPermissionService: AllowedPermissionService) : PermissionManager {
    override fun assignPermissionAttribute(item: Item, itemRec: ItemRec) {
        val permissionId = itemRec.permission
        val allowedPermissions = allowedPermissionService.findAllByItemName(item.name)
        if (permissionId == null) {
            itemRec.permission = allowedPermissions.find { it.isDefault }?.targetId ?: Permission.DEFAULT_PERMISSION_ID
        } else {
            val allowedPermissionIds = allowedPermissions.asSequence().map { it.targetId }.toSet() + Permission.DEFAULT_PERMISSION_ID
            if (permissionId !in allowedPermissionIds)
                throw IllegalArgumentException("Permission [$permissionId] is not allowed for item [${item.name}]")

            itemRec.permission = permissionId
        }
    }
}