package ru.scisolutions.scicmscore.schema.seeder

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.schema.model.DbSchema
import ru.scisolutions.scicmscore.schema.model.Item

@Service
class SchemaSeederImpl(
    private val schemaProps: SchemaProps,
    private val dbSchema: DbSchema,
    private val itemService: ItemService,
    private val schemaLockService: SchemaLockService,
    private val tableSeeder: TableSeeder,
    private val modelsApplier: ModelsApplier
) : SchemaSeeder {
    init {
        if (schemaProps.seedOnInit) {
            logger.info("Schema seed flag enabled. Trying to seed")
            seedSchema()
        }
    }

    final override fun seedSchema() {
        schemaLockService.lockOrThrow()

        val items = dbSchema.getItems()

        items.forEach { (_, item) -> modelsApplier.apply(item) }

        // Delete absent items
        if (schemaProps.deleteIfAbsent)
            deleteAbsentItems(items)

        schemaLockService.unlockOrThrow()
    }

    private fun deleteAbsentItems(items: Map<String, Item>) {
        val itemEntities = itemService.findAll()
        itemEntities
            .filter { it.name !in items }
            .forEach {
                tableSeeder.delete(it)
                itemService.delete(it)
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchemaSeederImpl::class.java)
    }
}