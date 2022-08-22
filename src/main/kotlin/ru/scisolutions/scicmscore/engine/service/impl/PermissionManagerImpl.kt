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
        val allowedPermissionIds = allowedPermissionService.findPermissionIdsByItemName(item.name)
        if (permissionId == null) {
            itemRec.permission = if (allowedPermissionIds.isEmpty()) Permission.DEFAULT_PERMISSION_ID else allowedPermissionIds[0]
        } else {
            if (permissionId !in allowedPermissionIds || permissionId != Permission.DEFAULT_PERMISSION_ID)
                throw IllegalArgumentException("Permission [$permissionId] is not allowed for item [${item.name}]")

            itemRec.permission = permissionId
        }
    }
}