package ru.scisolutions.scicmscore.dbschema

import ru.scisolutions.scicmscore.api.mapper.ItemMapper
import ru.scisolutions.scicmscore.api.model.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.service.PermissionService
import ru.scisolutions.scicmscore.entity.Item as ItemEntity

class DbSchemaSeeder(
    private val itemService: ItemService,
    private val permissionService: PermissionService
) {
    fun seedDbSchema(dbSchema: DbSchema) {
        for ((name, item) in dbSchema.getItems()) {
            var itemEntity = itemService.items[name]
            if (itemEntity == null) {
                // TODO: Add table

                itemEntity = itemMapper.map(item)
                // itemEntity.allowedPermissions.add(permissionService.defaultPermission)
                itemService.save(itemEntity)
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