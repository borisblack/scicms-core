package ru.scisolutions.scicmscore.schema.applier.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateCache
import ru.scisolutions.scicmscore.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.schema.mapper.ItemTemplateMapper
import ru.scisolutions.scicmscore.schema.model.AbstractModel
import ru.scisolutions.scicmscore.schema.model.ItemTemplate
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate as ItemTemplateEntity

@Service
class ItemTemplateApplier(
    private val schemaProps: SchemaProps,
    private val itemTemplateCache: ItemTemplateCache,
    // private val itemTemplateService: ItemTemplateService,
    private val itemService: ItemService,
    private val schemaLockService: SchemaLockService,
) : ModelApplier {
    override fun supports(clazz: Class<*>): Boolean = clazz == ItemTemplate::class.java

    override fun apply(model: AbstractModel): String {
        if (model !is ItemTemplate)
            throw IllegalArgumentException("Unsupported type [${model::class.java.simpleName}]")

        validateModel(model)

        val name = model.metadata.name
        var itemTemplateEntity = itemTemplateCache[name]
        if (itemTemplateEntity == null) {
            // schemaLockService.lockOrThrow()

            if (model.checksum == null && !itemService.canCreate(ItemEntity.ITEM_TEMPLATE_ITEM_NAME)) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You has no CREATE permission for [${ItemEntity.ITEM_TEMPLATE_ITEM_NAME}] item")
            }

            // Add item template
            logger.info("Creating the item template [{}]", name)
            itemTemplateEntity = itemTemplateMapper.map(model)

            itemTemplateCache[name] = itemTemplateEntity

            // schemaLockService.unlockOrThrow()
        } else if (isChanged(model, itemTemplateEntity)) {
            // schemaLockService.lockOrThrow()

            if (model.checksum == null && (itemTemplateEntity.core || itemService.findByNameForWrite(name) == null)) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You cannot update [$name] item template")
            }

            logger.info("Updating the item template [{}]", itemTemplateEntity.name)
            itemTemplateMapper.copy(model, itemTemplateEntity)
            itemTemplateCache[name] = itemTemplateEntity

            // schemaLockService.unlockOrThrow()
        } else {
            logger.info("Item template [{}] is unchanged. Nothing to update", itemTemplateEntity.name)
        }

        return itemTemplateEntity.id
    }

    private fun validateModel(model: ItemTemplate) {
        logger.info("Validating model [{}]", model.metadata.name)
        if(model.metadata.name.first().isUpperCase())
            throw IllegalArgumentException("Model name [${model.metadata.name}] must start with a lowercase character")

        model.spec.attributes.asSequence()
            .filter { (_, attribute) -> attribute.type == FieldType.relation }
            .forEach { (_, attribute) -> attribute.validate() }
    }

    private fun isChanged(itemTemplate: ItemTemplate, existingItemTemplateEntity: ItemTemplateEntity): Boolean {
        logger.debug("Checking changes for item template [{}]", existingItemTemplateEntity.name)

        val isFileExist = itemTemplate.checksum != null
        val ignoreFileChecksum = !schemaProps.useFileChecksum
        val isFileChanged = itemTemplate.checksum != existingItemTemplateEntity.checksum
        if (isFileExist) {
            if (ignoreFileChecksum) {
                logger.debug("Ignoring file checksum")
            } else if (isFileChanged) {
                logger.warn(
                    "Checksum for item template [{}] is different in database ({}) and file ({}).",
                    existingItemTemplateEntity.name, existingItemTemplateEntity.checksum, itemTemplate.checksum
                )
            }
        }

        if (isFileExist && !ignoreFileChecksum && !isFileChanged)
            return false

        val isHashChanged = itemTemplate.hashCode().toString() != existingItemTemplateEntity.hash
        if (isHashChanged) {
            logger.warn(
                "Hash for item template [{}] in database is {}, but now is {}.",
                existingItemTemplateEntity.name, existingItemTemplateEntity.hash, itemTemplate.hashCode()
            )
        }

        return isHashChanged
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemTemplateApplier::class.java)
        private val itemTemplateMapper = ItemTemplateMapper()
    }
}