package ru.scisolutions.scicmscore.schema.applier.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.model.ItemSpec
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateCache
import ru.scisolutions.scicmscore.persistence.service.SchemaLockService
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
    private val itemTemplateCache: ItemTemplateCache,
    private val itemCache: ItemCache,
    private val itemMapper: ItemMapper,
    private val itemService: ItemService,
    private val tableSeeder: TableSeeder,
    private val schemaLockService: SchemaLockService,
    private val relationValidator: RelationValidator
) : ModelApplier {
    override fun supports(clazz: Class<*>): Boolean = clazz == Item::class.java

    override fun apply(model: AbstractModel): String {
        if (model !is Item)
            throw IllegalArgumentException("Unsupported type [${model::class.java.simpleName}]")

        val item = includeTemplates(model)

        validateModel(item)

        val name = item.metadata.name
        var itemEntity = itemCache[name]
        if (itemEntity == null) {
            // schemaLockService.lockOrThrow()

            if (item.checksum == null && !itemService.canCreate(ItemEntity.ITEM_ITEM_NAME)) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You has no CREATE permission for [${ItemEntity.ITEM_ITEM_NAME}] item")
            }

            tableSeeder.create(item) // create table

            // Add item
            logger.info("Creating the item [{}]", name)
            itemEntity = itemMapper.mapToEntity(item)

            itemCache[name] = itemEntity

            // schemaLockService.unlockOrThrow()
        } else if (isChanged(itemEntity, item)) {
            // schemaLockService.lockOrThrow()

            if (item.checksum == null && (itemEntity.core || itemService.findByNameForWrite(name) == null)) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You has no WRITE permission for [$name] item.")
            }

            if (item.metadata.dataSource != itemEntity.ds) {
                schemaLockService.unlockOrThrow()
                throw IllegalArgumentException("Item [${name}] datasource cannot be changed")
            }

            tableSeeder.update(item, itemEntity) // update table

            logger.info("Updating the item [{}]", itemEntity.name)
            itemMapper.copyToEntity(item, itemEntity)
            itemCache[name] = itemEntity

            // schemaLockService.unlockOrThrow()
        } else {
            logger.info("Item [{}] is unchanged. Nothing to update.", itemEntity.name)
        }

        return itemEntity.id
    }

    private fun includeTemplates(item: Item): Item {
        var mergedItem: Item = item
        for (templateName in item.includeTemplates) {
            val itemTemplate = itemTemplateCache.getOrThrow(templateName)
            mergedItem = includeTemplate(mergedItem, itemTemplate)
        }
        return mergedItem
    }

    private fun includeTemplate(item: Item, itemTemplateEntity: ItemTemplate) = Item(
        coreVersion = item.coreVersion,
        metadata = item.metadata,
        checksum = item.checksum,
        includeTemplates = item.includeTemplates,
        spec = mergeSpec(itemTemplateEntity.spec, item.spec)
    )

    private fun mergeSpec(from: ItemSpec, to: ItemSpec) = ItemSpec(
        attributes = Maps.merge(from.attributes, to.attributes),
        indexes = Maps.merge(from.indexes, to.indexes)
    )

    private fun validateModel(model: Item) {
        logger.info("Validating model [{}]", model.metadata.name)
        val metadata = model.metadata
        if(metadata.name.first().isUpperCase())
            throw IllegalArgumentException("Model name [${metadata.name}] must start with a lowercase character.")

        // Check if item implementation exists
        if (metadata.implementation != null) {
            Class.forName(metadata.implementation)
        }

        model.spec.attributes
            .forEach { (attrName, attribute) ->
                attribute.validate()

                if (attribute.type == FieldType.relation)
                    relationValidator.validateAttribute(model, attrName, attribute)

                // Sequence might not exist while item is creating
                // if (attribute.type == FieldType.sequence && !sequenceService.existsByName(requireNotNull(attribute.seqName)))
                //     throw IllegalArgumentException("Sequence [${attribute.seqName}] does not exist")
            }

    }

    private fun isChanged(existingItemEntity: ItemEntity, item: Item): Boolean {
        logger.debug("Checking changes for item [{}]", existingItemEntity.name)

        val isFileExist = item.checksum != null
        val ignoreFileChecksum = !schemaProps.useFileChecksum
        val isFileChanged = item.checksum != existingItemEntity.checksum
        if (isFileExist) {
            if (ignoreFileChecksum) {
                logger.debug("Ignoring file checksum")
            } else if (isFileChanged) {
                logger.warn(
                    "Checksum for item [{}] is different in database ({}) and file ({}).",
                    existingItemEntity.name, existingItemEntity.checksum, item.checksum
                )
            }
        }

        if (isFileExist && !ignoreFileChecksum && !isFileChanged)
            return false

        val isHashChanged = item.hashCode().toString() != existingItemEntity.hash
        if (isHashChanged) {
            logger.warn(
                "Hash for item [{}] in database is {}, but now is {}.",
                existingItemEntity.name, existingItemEntity.hash, item.hashCode()
            )
        }

        return isHashChanged
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemApplier::class.java)
    }
}