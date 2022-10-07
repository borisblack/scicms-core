package ru.scisolutions.scicmscore.engine.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.service.AllowedPermissionCache

@Service
class PermissionManagerImpl(private val allowedPermissionCache: AllowedPermissionCache) : PermissionManager {
    override fun assignPermissionAttribute(item: Item, itemRec: ItemRec) {
        itemRec.permission = checkPermissionId(item, itemRec.permission)
    }

    override fun checkPermissionId(item: Item, permissionId: String?): String {
        val allowedPermissions = allowedPermissionCache[item.name]
        return if (permissionId == null) {
            allowedPermissions.find { it.isDefault }?.targetId ?: Permission.DEFAULT_PERMISSION_ID
        } else {
            val allowedPermissionIds = allowedPermissions.asSequence().map { it.targetId }.toSet() + Permission.DEFAULT_PERMISSION_ID
            if (permissionId !in allowedPermissionIds)
                throw IllegalArgumentException("Permission [$permissionId] is not allowed for item [${item.name}]")

            permissionId
        }
    }
}