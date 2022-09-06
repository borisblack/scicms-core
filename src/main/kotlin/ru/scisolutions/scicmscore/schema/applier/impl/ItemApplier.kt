package ru.scisolutions.scicmscore.schema.applier.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.ItemSpec
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateService
import ru.scisolutions.scicmscore.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.persistence.service.SequenceService
import ru.scisolutions.scicmscore.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.schema.mapper.ItemMapper
import ru.scisolutions.scicmscore.schema.model.AbstractModel
import ru.scisolutions.scicmscore.schema.model.Item
import ru.scisolutions.scicmscore.schema.service.RelationValidator
import ru.scisolutions.scicmscore.schema.service.TableSeeder
import ru.scisolutions.scicmscore.util.Maps
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class ItemApplier(
    private val schemaProps: SchemaProps,
    private val itemTemplateService: ItemTemplateService,
    private val itemService: ItemService,
    private val tableSeeder: TableSeeder,
    private val schemaLockService: SchemaLockService,
    private val sequenceService: SequenceService,
    private val relationValidator: RelationValidator
) : ModelApplier {
    override fun supports(clazz: Class<*>): Boolean = clazz == Item::class.java

    override fun apply(model: AbstractModel): String {
        if (model !is Item)
            throw IllegalArgumentException("Unsupported type [${model::class.java.simpleName}]")

        val item = includeTemplates(model)

        validateModel(item)

        val itemName = item.metadata.name
        var itemEntity = itemService.findByName(itemName)
        if (itemEntity == null) {
            // schemaLockService.lockOrThrow()

            if (item.checksum == null && itemService.findByNameForCreate(itemName) == null) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You has no CREATE permission for [$itemName] item")
            }

            tableSeeder.create(item) // create table

            // Add item
            logger.info("Creating the item [{}]", itemName)
            itemEntity = itemMapper.map(item)

            itemService.save(itemEntity)

            // schemaLockService.unlockOrThrow()
        } else if (isChanged(item, itemEntity)) {
            // schemaLockService.lockOrThrow()

            if (item.checksum == null && itemService.findByNameForWrite(itemName) == null) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You has no WRITE permission for [$itemName] item")
            }

            tableSeeder.update(item, itemEntity) // update table

            logger.info("Updating the item [{}]", itemEntity.name)
            itemMapper.copy(item, itemEntity)
            itemService.save(itemEntity)

            // schemaLockService.unlockOrThrow()
        } else {
            logger.info("Item [{}] is unchanged. Nothing to update", itemEntity.name)
        }

        return itemEntity.id
    }

    private fun includeTemplates(item: Item): Item {
        var mergedItem: Item = item
        for (templateName in item.includeTemplates) {
            val itemTemplate = itemTemplateService.getByName(templateName)
            mergedItem = includeTemplate(mergedItem, itemTemplate)
        }
        return mergedItem
    }

    private fun includeTemplate(item: Item, itemTemplateEntity: ItemTemplate) = Item(
        coreVersion = item.coreVersion,
        metadata = item.metadata,
        checksum = item.checksum,
        includeTemplates = item.includeTemplates,
        spec = mergeSpec(item.spec, itemTemplateEntity.spec)
    )

    private fun mergeSpec(source: ItemSpec, target: ItemSpec) = ItemSpec(
        attributes = Maps.merge(source.attributes, target.attributes),
        indexes = Maps.merge(source.indexes, target.indexes)
    )

    private fun validateModel(model: Item) {
        logger.info("Validating model [{}]", model.metadata.name)
        if(model.metadata.name.first().isUpperCase())
            throw IllegalArgumentException("Model name [${model.metadata.name}] must start with a lowercase character")

        // Check if item implementation exists
        if (model.metadata.implementation != null) {
            Class.forName(model.metadata.implementation)
        }

        model.spec.attributes
            .forEach { (attrName, attribute) ->
                attribute.validate()

                if (attribute.type == Attribute.Type.relation)
                    relationValidator.validateAttribute(model, attrName, attribute)

                // Sequence might not exist while item is creating
                // if (attribute.type == AttrType.sequence && !sequenceService.existsByName(requireNotNull(attribute.seqName)))
                //     throw IllegalArgumentException("Sequence [${attribute.seqName}] does not exist")
            }

    }

    private fun isChanged(item: Item, existingItemEntity: ItemEntity): Boolean =
        (!schemaProps.useFileChecksum || item.checksum == null || item.checksum != existingItemEntity.checksum) &&
            item.hashCode().toString() != existingItemEntity.hash

    companion object {
        private val logger = LoggerFactory.getLogger(ItemApplier::class.java)
        private val itemMapper = ItemMapper()
    }
}