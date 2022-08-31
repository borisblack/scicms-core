package ru.scisolutions.scicmscore.engine.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.service.AllowedPermissionService
import java.util.UUID

@Service
class PermissionManagerImpl(private val allowedPermissionService: AllowedPermissionService) : PermissionManager {
    override fun assignPermissionAttribute(item: Item, itemRec: ItemRec) {
        val permissionId = itemRec.permission
        val allowedPermissions = allowedPermissionService.findAllByItemName(item.name)
        if (permissionId == null) {
            val defaultAllowedPermission = allowedPermissions.find { it.isDefault }
            itemRec.permission = if (defaultAllowedPermission == null) Permission.DEFAULT_PERMISSION_ID else UUID.fromString(defaultAllowedPermission.targetId)
        } else {
            val allowedPermissionIds = allowedPermissions.asSequence().map { UUID.fromString(it.targetId) }.toSet()
            if (permissionId !in allowedPermissionIds && permissionId != Permission.DEFAULT_PERMISSION_ID)
                throw IllegalArgumentException("Permission [$permissionId] is not allowed for item [${item.name}]")

            itemRec.permission = permissionId
        }
    }
}