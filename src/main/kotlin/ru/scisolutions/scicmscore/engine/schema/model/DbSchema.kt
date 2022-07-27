package ru.scisolutions.scicmscore.engine.schema.model

class DbSchema {
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
                itemTemplates[metadata.name] = model
            }
            is Item -> {
                items[metadata.name] = model
            }
            else -> throw IllegalArgumentException("${model::class.simpleName} model type is not supported")
        }
    }

    fun getTemplateOrThrow(templateName: String): ItemTemplate =
        itemTemplates[templateName] ?: throw IllegalStateException("Template [$templateName] not found")

    fun getItemOrThrow(itemName: String): Item =
        items[itemName] ?: throw IllegalArgumentException("Item [$itemName] not found")

    fun getItemIncludeTemplates(name: String): Item {
        val itemModel = getItemOrThrow(name)
        return includeTemplates(itemModel)
    }

    fun getItemsIncludeTemplates(): Map<String, Item> =
        items.mapValues { (_, value) -> includeTemplates(value) }

    private fun includeTemplates(item: Item): Item {
        var mergedItem = item
        for (templateName in item.includeTemplates) {
            val itemTemplate = getTemplateOrThrow(templateName)
            mergedItem = mergedItem.includeTemplate(itemTemplate)
        }
        return mergedItem
    }
}