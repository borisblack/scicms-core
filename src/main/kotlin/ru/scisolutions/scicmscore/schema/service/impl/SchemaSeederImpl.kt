package ru.scisolutions.scicmscore.schema.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateService
import ru.scisolutions.scicmscore.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.schema.model.DbSchema
import ru.scisolutions.scicmscore.schema.model.Item
import ru.scisolutions.scicmscore.schema.model.ItemTemplate
import ru.scisolutions.scicmscore.schema.service.SchemaReader
import ru.scisolutions.scicmscore.schema.service.SchemaSeeder
import ru.scisolutions.scicmscore.schema.service.TableSeeder

@Service
class SchemaSeederImpl(
    private val schemaProps: SchemaProps,
    private val itemTemplateService: ItemTemplateService,
    private val itemService: ItemService,
    private val schemaLockService: SchemaLockService,
    private val schemaReader: SchemaReader,
    private val tableSeeder: TableSeeder,
    private val modelsApplier: ModelsApplier
) : SchemaSeeder {
    init {
        if (schemaProps.seedOnInit) {
            logger.info("Schema seed flag enabled. Trying to seed")
            seedSchema(schemaReader.read())
        }
    }

    final override fun seedSchema(dbSchema: DbSchema) {
        schemaLockService.lockOrThrow()

        // Process item templates
        val itemTemplates = dbSchema.getItemTemplates()
        itemTemplates.forEach { (_, itemTemplate) -> modelsApplier.apply(itemTemplate) }
        if (schemaProps.deleteIfAbsent)
            deleteAbsentItemTemplates(itemTemplates)

        // Process items
        val items = dbSchema.getItems()
        items.forEach { (_, item) -> modelsApplier.apply(item) }
        if (schemaProps.deleteIfAbsent)
            deleteAbsentItems(items)

        schemaLockService.unlockOrThrow()
    }

    private fun deleteAbsentItemTemplates(itemTemplates: Map<String, ItemTemplate>) {
        val itemTemplateEntities = itemTemplateService.findAll()
        itemTemplateEntities
            .filter { it.name !in itemTemplates }
            .forEach { itemTemplateService.delete(it) }
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