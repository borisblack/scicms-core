package ru.scisolutions.scicmscore.engine.schema.applier.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.ItemSpec
import ru.scisolutions.scicmscore.engine.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.persistence.service.ItemTemplateService
import ru.scisolutions.scicmscore.engine.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.engine.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.engine.schema.mapper.ItemMapper
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.model.ModelApplyResult
import ru.scisolutions.scicmscore.engine.schema.service.RelationValidator
import ru.scisolutions.scicmscore.engine.schema.service.TableSeeder
import ru.scisolutions.scicmscore.util.Maps
import ru.scisolutions.scicmscore.engine.persistence.entity.Item as ItemEntity

@Service
class ItemApplier(
    private val schemaProps: SchemaProps,
    private val itemTemplateService: ItemTemplateService,
    private val itemMapper: ItemMapper,
    private val itemService: ItemService,
    private val tableSeeder: TableSeeder,
    private val schemaLockService: SchemaLockService,
    private val relationValidator: RelationValidator
) : ModelApplier {
    override fun supports(clazz: Class<*>): Boolean = clazz == Item::class.java

    override fun apply(model: AbstractModel): ModelApplyResult {
        if (model !is Item) {
            throw IllegalArgumentException("Unsupported type [${model::class.java.simpleName}]")
        }

        val item = includeTemplates(model)

        validateModel(item)

        val name = item.metadata.name
        var itemEntity = itemService.findByName(name)
        if (itemEntity == null) {
            if (item.checksum == null && !itemService.canCreate(ItemEntity.ITEM_ITEM_NAME)) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You has no CREATE permission for [${ItemEntity.ITEM_ITEM_NAME}] item")
            }

            tableSeeder.create(item) // create table

            // Add item
            logger.info("Creating the item [{}]", name)
            itemEntity = itemMapper.mapToEntity(item)

            itemService.save(itemEntity)
            return ModelApplyResult(true, itemEntity.id)
        } else if (isChanged(itemEntity, item)) {
            if (item.checksum == null && (itemEntity.core || (itemService.findByNameForWrite(name)) == null)) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You has no WRITE permission for [$name] item.")
            }

//            if (item.metadata.dataSource != itemEntity.ds) {
//                schemaLockService.unlockOrThrow()
//                throw IllegalArgumentException("Item [${name}] datasource cannot be changed.")
//            }

            tableSeeder.update(item, itemEntity) // update table

            logger.info("Updating the item [{}]", itemEntity.name)
            itemMapper.copyToEntity(item, itemEntity)
            itemEntity.lockedById = null
            itemService.save(itemEntity)

            // schemaLockService.unlockOrThrow()
            return ModelApplyResult(true, itemEntity.id)
        } else {
            logger.info("Item [{}] is unchanged. Nothing to update.", itemEntity.name)
            return ModelApplyResult(false, itemEntity.id)
        }
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
        spec = mergeSpec(itemTemplateEntity.spec, item.spec)
    )

    private fun mergeSpec(from: ItemSpec, to: ItemSpec) = ItemSpec(
        attributes = Maps.merge(from.attributes, to.attributes),
        indexes = Maps.merge(from.indexes, to.indexes)
    )

    private fun validateModel(model: Item) {
        logger.info("Validating model [{}]", model.metadata.name)
        val metadata = model.metadata
        if (metadata.name.first().isUpperCase()) {
            throw IllegalArgumentException("Model name [${metadata.name}] must start with a lowercase character.")
        }

        if (model.metadata.pluralName.first().isUpperCase()) {
            throw IllegalArgumentException("Model plural name [${model.metadata.pluralName}] must start with a lowercase character.")
        }

        if (model.metadata.name == model.metadata.pluralName) {
            throw IllegalArgumentException("Model name and plural name cannot be equal.")
        }

        if (model.metadata.tableName.isNullOrBlank() && model.metadata.query.isNullOrBlank()) {
            throw IllegalArgumentException("Model table and query are empty.")
        }

        if (model.metadata.performDdl && model.metadata.tableName.isNullOrBlank()) {
            throw IllegalArgumentException("Model table is empty, so DDL cannot be performed.")
        }

        // Check if item implementation exists
        if (!metadata.implementation.isNullOrBlank()) {
            Class.forName(metadata.implementation)
        }

        if (metadata.idAttribute !in model.spec.attributes) {
            throw IllegalArgumentException("Item has not ID attribute.")
        }

        model.spec.attributes
            .forEach { (attrName, attribute) ->
                attribute.validate()

                if (attribute.type == FieldType.relation) {
                    relationValidator.validateAttribute(model, attrName, attribute)
                }

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
                    existingItemEntity.name,
                    existingItemEntity.checksum,
                    item.checksum
                )
            }
        }

        if (isFileExist && !ignoreFileChecksum && !isFileChanged) {
            return false
        }

        val isHashChanged = item.hashCode().toString() != existingItemEntity.hash
        if (isHashChanged) {
            logger.warn(
                "Hash for item [{}] in database is {}, but now is {}.",
                existingItemEntity.name,
                existingItemEntity.hash,
                item.hashCode()
            )
        }

        return isHashChanged
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemApplier::class.java)
    }
}
