package ru.scisolutions.scicmscore.engine.handler.util

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.PermissionCache
import ru.scisolutions.scicmscore.util.Acl.Mask

@Component
class AclHelper(
    private val permissionCache: PermissionCache
) {
    fun canRead(itemRec: ItemRec) =
        hasAccess(itemRec, Mask.READ)

    fun canRead(permissionId: String) =
        hasAccess(permissionId, Mask.READ)

    fun canWrite(itemRec: ItemRec) =
        hasAccess(itemRec, Mask.WRITE)

    fun canWrite(permissionId: String) =
        hasAccess(permissionId, Mask.WRITE)

    fun canCreate(item: Item) =
        hasAccess(item, Mask.CREATE)

    fun canCreate(permissionId: String) =
        hasAccess(permissionId, Mask.CREATE)

    fun canDelete(itemRec: ItemRec) =
        hasAccess(itemRec, Mask.DELETE)

    fun canDelete(permissionId: String) =
        hasAccess(permissionId, Mask.DELETE)

    fun canAdmin(itemRec: ItemRec) =
        hasAccess(itemRec, Mask.ADMINISTRATION)

    fun canAdmin(permissionId: String) =
        hasAccess(permissionId, Mask.ADMINISTRATION)

    fun hasAccess(item: Item, accessMask: Mask): Boolean {
        val permissionId = item.permissionId ?: return true
        return hasAccess(permissionId, accessMask)
    }

    fun hasAccess(itemRec: ItemRec, accessMask: Mask): Boolean {
        val permissionId = itemRec.permission ?: return true
        return hasAccess(permissionId, accessMask)
    }

    fun hasAccess(permissionId: String, accessMask: Mask): Boolean =
        permissionId in permissionCache.idsByAccessMask(accessMask)
}