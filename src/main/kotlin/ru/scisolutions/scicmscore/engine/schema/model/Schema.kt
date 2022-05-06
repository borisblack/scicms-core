package ru.scisolutions.scicmscore.engine.schema.model

import org.slf4j.LoggerFactory

class Schema {
    private val itemTemplates = mutableMapOf<String, ItemTemplate>()
    private val items = mutableMapOf<String, Item>()

    fun addModels(models: Collection<AbstractModel>) {
        for (model in models) {
            addModel(model)
        }
    }

    fun addModel(model: AbstractModel) {
        val metadata = model.metadata
        if(metadata.name.first().isUpperCase())
            throw IllegalArgumentException("Model name [${metadata.name}] must start with a lowercase character")

        when(model) {
            is ItemTemplate -> {
                logger.info("Validating item template [{}]", metadata.name)
                model.spec.validate()
                itemTemplates[metadata.name] = model
            }
            is Item -> {
                logger.info("Validating item [{}]", metadata.name)
                model.spec.validate()
                items[metadata.name] = model
            }
            else -> throw IllegalArgumentException("${model::class.simpleName} model type is not supported")
        }
    }

    fun getItemIncludeTemplates(name: String): Item {
        val itemModel = items[name] ?: throw IllegalArgumentException("Item $name not found")
        return includeTemplates(itemModel)
    }

    fun getItemsIncludeTemplates(): Map<String, Item> =
        items.mapValues { (_, value) -> includeTemplates(value) }

    private fun includeTemplates(item: Item): Item {
        var mergedItem = item
        for (templateName in item.includeTemplates) {
            val itemTemplate = itemTemplates[templateName] ?: throw IllegalStateException("Template $templateName not found")
            mergedItem = mergedItem.includeTemplate(itemTemplate)
        }
        return mergedItem
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Schema::class.java)
    }
}