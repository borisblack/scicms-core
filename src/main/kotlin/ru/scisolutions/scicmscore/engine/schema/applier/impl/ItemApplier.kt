package ru.scisolutions.scicmscore.engine.schema.applier.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.engine.schema.mapper.ItemMapper
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.seeder.TableSeeder
import ru.scisolutions.scicmscore.engine.schema.service.impl.RelationValidator
import ru.scisolutions.scicmscore.service.ItemLockService
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class ItemApplier(
    private val itemService: ItemService,
    private val tableSeeder: TableSeeder,
    private val itemLockService: ItemLockService,
    private val relationValidator: RelationValidator
) : ModelApplier {
    override fun supports(clazz: Class<*>): Boolean = clazz == Item::class.java

    override fun apply(model: AbstractModel) {
        if (model !is Item)
            throw IllegalArgumentException("Unsupported type [${model::class.java.simpleName}]")

        validateItem(model)

        var itemEntity = itemService.findByName(model.metadata.name)
        if (itemEntity == null) {
            // itemLockService.lockOrThrow()

            tableSeeder.create(model) // create table

            // Add item
            logger.info("Creating the item [{}]", model.metadata.name)
            itemEntity = itemMapper.map(model)

            itemService.save(itemEntity)

            // itemLockService.unlockOrThrow()
        } else if (isChanged(model, itemEntity)) {
            // itemLockService.lockOrThrow()

            tableSeeder.update(model, itemEntity) // update table

            logger.info("Updating the item [{}]", itemEntity.name)
            itemMapper.copy(model, itemEntity)
            itemService.save(itemEntity)

            // itemLockService.unlockOrThrow()
        } else {
            logger.info("Item [{}] is unchanged. Nothing to update", itemEntity.name)
        }
    }

    private fun validateItem(item: Item) {
        logger.info("Validating item [{}]", item.metadata.name)
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> attribute.type == Attribute.Type.relation }
            .forEach { (attrName, attribute) ->
                // logger.debug("Validating attribute [{}]", attrName)
                attribute.validate()
                relationValidator.validateAttribute(item, attrName, attribute)
            }

        // Check if item implementation exists
        if (item.metadata.implementation != null) {
            Class.forName(item.metadata.implementation)
        }
    }

    private fun isChanged(item: Item, existingItemEntity: ItemEntity): Boolean =
        (item.checksum == null || item.checksum != existingItemEntity.checksum) && item.hashCode().toString() != existingItemEntity.hash

    companion object {
        private val logger = LoggerFactory.getLogger(ItemApplier::class.java)
        private val itemMapper = ItemMapper()
    }
}