package ru.scisolutions.scicmscore.engine.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AclHelper
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.service.AllowedPermissionCache

@Service
class PermissionManagerImpl(
    private val allowedPermissionCache: AllowedPermissionCache,
    private val aclHelper: AclHelper
) : PermissionManager {
    override fun assignPermissionAttribute(item: Item, itemRec: ItemRec) {
        itemRec.permission = checkPermissionId(item, itemRec.permission)
    }

    override fun assignPermissionAttribute(item: Item, prevItemRec: ItemRec, itemRec: ItemRec) {
        itemRec.permission = checkPermissionId(item, prevItemRec.permission, itemRec.permission)
    }

    override fun checkPermissionId(item: Item, permissionId: String?): String {
        val allowedPermissions = allowedPermissionCache[item.name]

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

    override fun checkPermissionId(item: Item, prevPermissionId: String?, permissionId: String?): String {
        val allowedPermissions = allowedPermissionCache[item.name]
        val defaultPermission = getDefaultPermission(item.name)
        val effectiveDefaultPermission = allowedPermissions.find { it.isDefault }?.targetId ?: defaultPermission

        if (permissionId == null)
            return prevPermissionId ?: effectiveDefaultPermission

        if (!aclHelper.canAdmin(prevPermissionId)) {
            logger.warn("User cannot change permission")
            return prevPermissionId ?: effectiveDefaultPermission
        }

        val allowedPermissionIds = allowedPermissions.asSequence().map { it.targetId }.toSet() + defaultPermission
        return if (permissionId in allowedPermissionIds) {
            permissionId
        } else {
            logger.warn("Permission '$permissionId' is not allowed for item '${item.name}'. Resetting to default permission '$effectiveDefaultPermission'")
            effectiveDefaultPermission
        }
    }

    private fun getDefaultPermission(itemName: String): String {
        if (isSecurityItem(itemName))
            return Permission.SECURITY_PERMISSION_ID

        if (isBiItem(itemName))
            return Permission.BI_PERMISSION_ID

        return Permission.DEFAULT_PERMISSION_ID
    }

    private fun isSecurityItem(itemName: String) = itemName in securityItemNames

    private fun isBiItem(itemName: String) = itemName in biItemNames

    companion object {
        private val logger = LoggerFactory.getLogger(PermissionManagerImpl::class.java)
        private val securityItemNames = setOf(
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
        private val biItemNames = setOf(Item.DASHBOARD_ITEM_NAME, Item.DATASET_ITEM_NAME)
    }
}