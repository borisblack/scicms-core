package ru.scisolutions.scicmscore.engine.schema.seeder

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.mapper.ItemMapper
import ru.scisolutions.scicmscore.engine.schema.model.DbSchema
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.seeder.liquibase.LiquibaseTableSeeder
import ru.scisolutions.scicmscore.engine.schema.service.impl.RelationValidator
import ru.scisolutions.scicmscore.service.ItemLockService
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class SchemaSeederImpl(
    private val schemaProps: SchemaProps,
    private val dbSchema: DbSchema,
    private val relationValidator: RelationValidator,
    private val itemService: ItemService,
    private val itemLockService: ItemLockService,
    private val tableSeeder: TableSeeder
) : SchemaSeeder {
    private var itemsLocked: Boolean = false

    init {
        if (schemaProps.seedOnInit) {
            logger.info("Schema seed flag enabled. Trying to seed")
            seedSchema()
        }
    }

    final override fun seedSchema() {
        val items = dbSchema.getItemsIncludeTemplates()
        items.forEach { (_, item) -> seedItem(item) }

        // Delete absent items
        if (schemaProps.deleteIfAbsent)
            deleteAbsentItems(items)

        releaseLock()
    }

    private fun acquireLock() {
        if (!itemsLocked) {
            if (!itemLockService.lock())
                throw IllegalStateException("Cannot acquire items lock")

            itemsLocked = true
            logger.info("Successfully acquired items lock")
        }
    }

    private fun releaseLock() {
        if (itemsLocked) {
            if (!itemLockService.unlock())
                throw IllegalStateException("Cannot release items lock")

            itemsLocked = false
            logger.info("Successfully released items lock")
        }
    }

    override fun seedItem(item: Item) {
        validateItem(item)

        var itemEntity = itemService.findByName(item.metadata.name)
        if (itemEntity == null) {
            acquireLock()

            tableSeeder.create(item) // create table

            // Add item
            logger.info("Creating the item [{}]", item.metadata.name)
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
            acquireLock()

            tableSeeder.update(item, itemEntity) // update table

            logger.info("Updating the item [{}]", itemEntity.name)
            itemMapper.copy(item, itemEntity)
            itemService.save(itemEntity)
        } else {
            logger.info("Item [{}] is unchanged. Nothing to update", itemEntity.name)
        }
    }

    private fun validateItem(item: Item) {
        logger.info("Validating item [${item.metadata.name}]")
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> attribute.type == Type.relation }
            .forEach { (attrName, attribute) ->
                // logger.debug("Validating attribute [$attrName]")
                attribute.validate()
                relationValidator.validateAttribute(item, attrName, attribute)
            }
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
        private val logger = LoggerFactory.getLogger(SchemaSeederImpl::class.java)
        private val itemMapper = ItemMapper()
    }
}