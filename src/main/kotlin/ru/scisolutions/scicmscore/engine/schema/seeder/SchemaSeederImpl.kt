package ru.scisolutions.scicmscore.engine.schema.seeder

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.engine.schema.mapper.ItemMapper
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class SchemaSeederImpl(
    private val schemaProps: SchemaProps,
    private val itemService: ItemService,
    private val itemSeeder: ItemSeeder
) : SchemaSeeder {
    override fun seed(items: Map<String, Item>) {
        // Create/update items
        seedItems(items)

        // Delete absent items
        if (schemaProps.deleteIfAbsent)
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