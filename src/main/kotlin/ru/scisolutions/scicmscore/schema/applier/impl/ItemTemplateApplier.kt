package ru.scisolutions.scicmscore.schema.applier.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateService
import ru.scisolutions.scicmscore.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.schema.mapper.ItemTemplateMapper
import ru.scisolutions.scicmscore.schema.model.AbstractModel
import ru.scisolutions.scicmscore.schema.model.ItemTemplate
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate as ItemTemplateEntity

@Service
class ItemTemplateApplier(
    private val schemaProps: SchemaProps,
    private val itemTemplateService: ItemTemplateService,
    private val schemaLockService: SchemaLockService,
) : ModelApplier {
    override fun supports(clazz: Class<*>): Boolean = clazz == ItemTemplate::class.java

    override fun apply(model: AbstractModel): String {
        if (model !is ItemTemplate)
            throw IllegalArgumentException("Unsupported type [${model::class.java.simpleName}]")

        validateModel(model)

        val name = model.metadata.name
        var itemTemplateEntity = itemTemplateService.findByName(name)
        if (itemTemplateEntity == null) {
            // schemaLockService.lockOrThrow()

            if (model.checksum == null) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("Item template can only be created from file")
            }

            // Add item template
            logger.info("Creating the item template [{}]", name)
            itemTemplateEntity = itemTemplateMapper.map(model)

            itemTemplateService.save(itemTemplateEntity)

            // schemaLockService.unlockOrThrow()
        } else if (isChanged(model, itemTemplateEntity)) {
            // schemaLockService.lockOrThrow()

            if (model.checksum == null) {
                schemaLockService.unlockOrThrow()
                throw AccessDeniedException("Item template can only be updated from file")
            }

            logger.info("Updating the item template [{}]", itemTemplateEntity.name)
            itemTemplateMapper.copy(model, itemTemplateEntity)
            itemTemplateService.save(itemTemplateEntity)

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
            .filter { (_, attribute) -> attribute.type == Attribute.Type.relation }
            .forEach { (_, attribute) -> attribute.validate() }
    }

    private fun isChanged(itemTemplate: ItemTemplate, existingItemTemplateEntity: ItemTemplateEntity): Boolean =
        (!schemaProps.useFileChecksum || itemTemplate.checksum == null || itemTemplate.checksum != existingItemTemplateEntity.checksum) &&
            itemTemplate.hashCode().toString() != existingItemTemplateEntity.hash

    companion object {
        private val logger = LoggerFactory.getLogger(ItemTemplateApplier::class.java)
        private val itemTemplateMapper = ItemTemplateMapper()
    }
}