package ru.scisolutions.scicmscore.engine.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AclHelper
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.entity.Permission
import ru.scisolutions.scicmscore.engine.persistence.service.AllowedPermissionService

@Service
class PermissionManager(
    private val allowedPermissionService: AllowedPermissionService,
    private val aclHelper: AclHelper
) {
    fun assignPermissionAttribute(item: Item, itemRec: ItemRec) {
        if (!item.hasPermissionAttribute()) {
            return
        }

        itemRec.permission = checkPermissionId(item, itemRec.permission)
    }

    fun assignPermissionAttribute(item: Item, prevItemRec: ItemRec, itemRec: ItemRec) {
        if (!item.hasPermissionAttribute()) {
            return
        }

        itemRec.permission = checkPermissionId(item, prevItemRec.permission, itemRec.permission)
    }

    fun checkPermissionId(item: Item, permissionId: String?): String {
        if (!item.hasPermissionAttribute()) {
            throw IllegalArgumentException("Item has no permission attribute.")
        }

        val allowedPermissions = allowedPermissionService.findAllByItemName(item.name)

        return if (permissionId == null) {
            allowedPermissions.find { it.isDefault }?.targetId ?: getDefaultPermission(item.name)
        } else {
            val defaultPermissionId = getDefaultPermission(item.name)
            val allowedPermissionIds = allowedPermissions.asSequence().map { it.targetId }.toSet() + defaultPermissionId
            if (permissionId in allowedPermissionIds) {
                permissionId
            } else {
                throw IllegalArgumentException("Permission '$permissionId' is not allowed for item '${item.name}'.")
                // logger.warn("Permission '$permissionId' is not allowed for item '${item.name}'. Resetting to default permission '$defaultPermission'")
                // defaultPermission
            }
        }
    }

    fun checkPermissionId(item: Item, prevPermissionId: String?, permissionId: String?): String {
        if (!item.hasPermissionAttribute()) {
            throw IllegalArgumentException("Item has no permission attribute.")
        }

        val allowedPermissions = allowedPermissionService.findAllByItemName(item.name)
        val defaultPermission = getDefaultPermission(item.name)
        val effectiveDefaultPermission = allowedPermissions.find { it.isDefault }?.targetId ?: defaultPermission

        if (permissionId == null) {
            return prevPermissionId ?: effectiveDefaultPermission
        }

        if (!aclHelper.canAdmin(prevPermissionId)) {
            logger.warn("User cannot change permission")
            return prevPermissionId ?: effectiveDefaultPermission
        }

        val allowedPermissionIds = allowedPermissions.asSequence().map { it.targetId }.toSet() + defaultPermission
        return if (permissionId in allowedPermissionIds) {
            permissionId
        } else {
            logger.warn(
                "Permission [$permissionId] is not allowed for item [${item.name}]. " +
                    "Resetting to default permission [$effectiveDefaultPermission]"
            )
            effectiveDefaultPermission
        }
    }

    private fun getDefaultPermission(itemName: String): String {
        if (isSecurityItem(itemName)) {
            return Permission.SECURITY_PERMISSION_ID
        }

        if (isBiItem(itemName)) {
            return Permission.BI_PERMISSION_ID
        }

        return Permission.DEFAULT_PERMISSION_ID
    }

    private fun isSecurityItem(itemName: String) = itemName in securityItemNames

    private fun isBiItem(itemName: String) = itemName in biItemNames

    companion object {
        private val logger = LoggerFactory.getLogger(PermissionManager::class.java)
        private val securityItemNames =
            setOf(
                // Item.ACCESS_ITEM_NAME,
                // Item.ALLOWED_PERMISSION_ITEM_NAME,
                Item.GROUP_ITEM_NAME,
                Item.GROUP_MEMBER_ITEM_NAME,
                Item.GROUP_ROLE_ITEM_NAME,
                // Item.IDENTITY_ITEM_NAME,
                // Item.PERMISSION_ITEM_NAME,
                Item.ROLE_ITEM_NAME,
                Item.USER_ITEM_NAME
            )
        private val biItemNames =
            setOf(
                Item.DASHBOARD_ITEM_NAME,
                Item.DASHBOARD_CATEGORY_ITEM_NAME,
                Item.DASHBOARD_CATEGORY_HIERARCHY_ITEM_NAME,
                Item.DASHBOARD_CATEGORY_MAP_ITEM_NAME,
                Item.DATASET_ITEM_NAME
            )
    }
}
