package ru.scisolutions.scicmscore.dbschema

import ru.scisolutions.scicmscore.api.mapper.ItemMapper
import ru.scisolutions.scicmscore.api.model.Item
import ru.scisolutions.scicmscore.entity.AllowedPermission
import ru.scisolutions.scicmscore.entity.Permission
import ru.scisolutions.scicmscore.service.AllowedPermissionService
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.entity.Item as ItemEntity

class DbSchemaSeeder(
    private val itemService: ItemService,
    private val allowedPermissionService: AllowedPermissionService
) {
    fun seedDbSchema(dbSchema: DbSchema) {
        for ((name, item) in dbSchema.getItems()) {
            var itemEntity = itemService.items[name]
            if (itemEntity == null) {
                // TODO: Add table

                // Add item
                itemEntity = itemMapper.map(item)
                // itemEntity.allowedPermissions.add(permissionService.defaultPermission)
                itemService.save(itemEntity)

                // Add default allowed permission
                // val defaultAllowedPermission = AllowedPermission(
                //     sourceId = itemEntity.id,
                //     targetId = Permission.DEFAULT_PERMISSION_ID,
                //     isDefault = true
                // )
                // allowedPermissionService.save(defaultAllowedPermission)
            } else if (isChanged(item, itemEntity)) {
                // TODO: Change table

                itemService.save(itemEntity)
            }
        }
    }

    private fun isChanged(item: Item, itemEntity: ItemEntity): Boolean =
        item.hashCode().toString() != itemEntity.checksum

    companion object {
        private val itemMapper = ItemMapper()
    }
}