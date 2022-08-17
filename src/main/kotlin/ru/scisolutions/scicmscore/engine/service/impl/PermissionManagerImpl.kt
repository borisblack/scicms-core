package ru.scisolutions.scicmscore.engine.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.AllowedPermissionService

@Service
class PermissionManagerImpl(private val allowedPermissionService: AllowedPermissionService) : PermissionManager {
    override fun assignPermissionAttribute(item: Item, itemRec: ItemRec) {
        val permissionId = itemRec.permission
        if (permissionId == null) {
            // itemRec.permission = Permission.DEFAULT_PERMISSION_ID
        } else {
            if (permissionId !in allowedPermissionService.findPermissionIdsByItemName(item.name))
                throw IllegalArgumentException("Permission [$permissionId] is not allowed for item [${item.name}]")

            itemRec.permission = permissionId
        }
    }
}