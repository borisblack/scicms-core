package ru.scisolutions.scicmscore.engine.schema.seeder

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.mapper.ItemMapper
import ru.scisolutions.scicmscore.engine.schema.model.DbSchema
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.relation.handler.RelationValidator
import ru.scisolutions.scicmscore.service.ItemLockService
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class DbSchemaSeederImpl(
    private val schemaProps: SchemaProps,
    private val dbSchema: DbSchema,
    private val relationValidator: RelationValidator,
    private val itemService: ItemService,
    private val itemLockService: ItemLockService,
    private val tableSeeder: TableSeeder
) : DbSchemaSeeder {
    private var itemsLocked: Boolean = false

    init {
        if (schemaProps.seedOnInit) {
            logger.info("Schema seed flag enabled. Trying to seed")
            seedSchema()
        }
    }

    final override fun seedSchema() {
        obtainLock()

        val items = dbSchema.getItemsIncludeTemplates()
        items.forEach { (_, item) -> seedItem(item) }

        // Delete absent items
        if (schemaProps.deleteIfAbsent)
            deleteAbsentItems(items)

        releaseLock()
    }

    private fun obtainLock() {
        if (!itemsLocked) {
            if (!itemLockService.lock())
                throw IllegalStateException("Cannot obtain items lock")

            itemsLocked = true
        }
    }

    private fun releaseLock() {
        if (itemsLocked) {
            if (!itemLockService.unlock())
                throw IllegalStateException("Cannot release items lock")

            itemsLocked = false
        }
    }

    override fun seedItem(item: Item) {
        validateItem(item)

        var itemEntity = itemService.findByName(item.metadata.name)

        // Create/update table
        if (itemEntity == null)
            tableSeeder.create(item) // create table
        else
            tableSeeder.update(item, itemEntity) // update table

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

    private fun validateItem(item: Item) {
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> attribute.type == Type.relation }
            .forEach { (attrName, _) -> relationValidator.validateAttribute(item, attrName) }
    }

    private fun isChanged(item: Item, itemEntity: ItemEntity): Boolean =
        item.hashCode().toString() != itemEntity.checksum

    private fun deleteAbsentItems(items: Map<String, Item>) {
        val itemEntities = itemService.findAll()
        val itemsToDelete = mutableListOf<ItemEntity>()
        for (itemEntity in itemEntities) {
            if (itemEntity.name !in items) {
                tableSeeder.delete(itemEntity) // drop table
                itemsToDelete.add(itemEntity)
            }
        }

        // Delete items
        for (itemEntity in itemsToDelete)
            itemService.delete(itemEntity)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DbSchemaSeederImpl::class.java)
        private val itemMapper = ItemMapper()
    }
}