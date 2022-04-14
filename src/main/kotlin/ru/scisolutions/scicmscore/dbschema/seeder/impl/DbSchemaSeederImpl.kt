package ru.scisolutions.scicmscore.dbschema.seeder.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.api.mapper.ItemMapper
import ru.scisolutions.scicmscore.api.model.Item
import ru.scisolutions.scicmscore.dbschema.DbSchema
import ru.scisolutions.scicmscore.dbschema.seeder.DbSchemaSeeder
import ru.scisolutions.scicmscore.dbschema.seeder.ItemSeeder
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.entity.Item as ItemEntity

@Service
class DbSchemaSeederImpl(
    private val itemService: ItemService,
    private val itemSeeder: ItemSeeder
) : DbSchemaSeeder {
    override fun seed(dbSchema: DbSchema) {
        for ((name, item) in dbSchema.getItems()) {
            var itemEntity = itemService.items[name]

            itemSeeder.seed(item, itemEntity) // create/update table

            if (itemEntity == null) {
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
                // Update item
                itemMapper.copy(item, itemEntity)
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