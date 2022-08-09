package ru.scisolutions.scicmscore.engine.schema.seeder

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.engine.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.engine.schema.model.DbSchema
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.service.ItemLockService
import ru.scisolutions.scicmscore.service.ItemService

@Service
class SchemaSeederImpl(
    private val schemaProps: SchemaProps,
    private val dbSchema: DbSchema,
    private val itemService: ItemService,
    private val itemLockService: ItemLockService,
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
        itemLockService.lockOrThrow()

        val items = dbSchema.getItems()
        items.forEach { (_, item) -> modelsApplier.apply(item) }

        // Delete absent items
        if (schemaProps.deleteIfAbsent)
            deleteAbsentItems(items)

        itemLockService.unlockOrThrow()
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