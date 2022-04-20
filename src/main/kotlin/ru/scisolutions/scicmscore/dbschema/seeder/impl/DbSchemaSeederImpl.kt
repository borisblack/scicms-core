package ru.scisolutions.scicmscore.dbschema.seeder.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.mapper.ItemMapper
import ru.scisolutions.scicmscore.domain.model.Item
import ru.scisolutions.scicmscore.dbschema.DbSchema
import ru.scisolutions.scicmscore.dbschema.seeder.DbSchemaSeeder
import ru.scisolutions.scicmscore.dbschema.seeder.ItemSeeder
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.entity.Item as ItemEntity

@Service
class DbSchemaSeederImpl(
    @Value("\${scicms-core.db-schema.delete-if-absent:false}")
    private val deleteIfAbsent: Boolean,
    private val itemService: ItemService,
    private val itemSeeder: ItemSeeder
) : DbSchemaSeeder {
    override fun seed(dbSchema: DbSchema) {
        val items = dbSchema.getItems()
        // Create/update items
        seedItems(items)

        // Delete absent items
        if (deleteIfAbsent)
            deleteAbsentItems(items)
    }

    private fun seedItems(items: Map<String, Item>) {
        for ((name, item) in items) {
            var itemEntity = itemService.items[name]

            // Create/update table
            if (itemEntity == null)
                itemSeeder.create(item) // create table
            else
                itemSeeder.update(item, itemEntity) // update table

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

    private fun deleteAbsentItems(items: Map<String, Item>) {
        val itemsToDelete = mutableListOf<ItemEntity>()
        for ((name, itemEntity) in itemService.items) {
            if (!items.containsKey(name)) {
                itemSeeder.delete(itemEntity) // drop table
                itemsToDelete.add(itemEntity)
            }
        }

        // Delete items
        for (itemEntity in itemsToDelete)
            itemService.delete(itemEntity)
    }

    companion object {
        private val itemMapper = ItemMapper()
    }
}