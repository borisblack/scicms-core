package ru.scisolutions.scicmscore.engine.schema.applier.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.persistence.service.ItemTemplateService
import ru.scisolutions.scicmscore.engine.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.engine.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.engine.schema.mapper.ItemTemplateMapper
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.ItemTemplate
import ru.scisolutions.scicmscore.engine.schema.model.ModelApplyResult
import ru.scisolutions.scicmscore.engine.persistence.entity.Item as ItemEntity
import ru.scisolutions.scicmscore.engine.persistence.entity.ItemTemplate as ItemTemplateEntity

@Service
class ItemTemplateApplier(
    private val schemaProps: SchemaProps,
    private val itemTemplateService: ItemTemplateService,
    private val itemService: ItemService,
    private val schemaLockService: SchemaLockService,
    private val itemTemplateMapper: ItemTemplateMapper
) : ModelApplier {
    override fun supports(clazz: Class<*>): Boolean = clazz == ItemTemplate::class.java

    override fun apply(model: AbstractModel): ModelApplyResult {
        if (model !is ItemTemplate) {
            throw IllegalArgumentException("Unsupported type [${model::class.java.simpleName}]")
        }

        validateModel(model)

        val name = model.metadata.name
        var itemTemplateEntity = itemTemplateService.findByName(name)
        if (itemTemplateEntity == null) {
            if (model.checksum == null && !itemService.canCreate(ItemEntity.ITEM_TEMPLATE_ITEM_NAME)) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You has no CREATE permission for [${ItemEntity.ITEM_TEMPLATE_ITEM_NAME}] item")
            }

            // Add item template
            logger.info("Creating the item template [{}]", name)
            itemTemplateEntity = itemTemplateMapper.map(model)

            itemTemplateService.save(itemTemplateEntity)

            // schemaLockService.unlockOrThrow()
            return ModelApplyResult(true, itemTemplateEntity.id)
        } else if (isChanged(model, itemTemplateEntity)) {
            if (model.checksum == null && (itemTemplateEntity.core || itemService.findByNameForWrite(name) == null)) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("You cannot update [$name] item template")
            }

            logger.info("Updating the item template [{}]", itemTemplateEntity.name)
            itemTemplateMapper.copy(model, itemTemplateEntity)
            itemTemplateEntity.lockedById = null
            itemTemplateService.save(itemTemplateEntity)

            return ModelApplyResult(true, itemTemplateEntity.id)
        } else {
            logger.info("Item template [{}] is unchanged. Nothing to update.", itemTemplateEntity.name)
            return ModelApplyResult(false, itemTemplateEntity.id)
        }
    }

    private fun validateModel(model: ItemTemplate) {
        logger.info("Validating model [{}]", model.metadata.name)
        if (model.metadata.name.first().isUpperCase()) {
            throw IllegalArgumentException("Model name [${model.metadata.name}] must start with a lowercase character.")
        }

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
                    existingItemTemplateEntity.name,
                    existingItemTemplateEntity.checksum,
                    itemTemplate.checksum
                )
            }
        }

        if (isFileExist && !ignoreFileChecksum && !isFileChanged) {
            return false
        }

        val isHashChanged = itemTemplate.hashCode().toString() != existingItemTemplateEntity.hash
        if (isHashChanged) {
            logger.warn(
                "Hash for item template [{}] in database is {}, but now is {}.",
                existingItemTemplateEntity.name,
                existingItemTemplateEntity.hash,
                itemTemplate.hashCode()
            )
        }

        return isHashChanged
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemTemplateApplier::class.java)
    }
}
