package ru.scisolutions.scicmscore.engine.schema.model

class DbSchema {
    private val itemTemplates = mutableMapOf<String, ItemTemplate>()
    private val items = mutableMapOf<String, Item>()

    fun putModels(models: Collection<AbstractModel>) {
        for (model in models) {
            putModel(model)
        }
    }

    fun putModel(model: AbstractModel) {
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

    fun getTemplate(templateName: String): ItemTemplate? = itemTemplates[templateName]

    fun getTemplateOrThrow(templateName: String): ItemTemplate =
        itemTemplates[templateName] ?: throw IllegalStateException("Template [$templateName] not found")

    fun getTemplates(): Map<String, ItemTemplate> = itemTemplates.toMap()

    fun listTemplates(): List<ItemTemplate> = itemTemplates.values.toList()

    fun getItem(itemName: String): Item? = items[itemName]

    fun getItemOrThrow(itemName: String): Item =
        getItem(itemName) ?: throw IllegalArgumentException("Item [$itemName] not found")

    fun getItemIncludeTemplates(name: String): Item? {
        val item = getItem(name) ?: return null
        return includeTemplates(item)
    }

    fun getItemIncludeTemplatesOrThrow(name: String): Item {
        val item = getItemOrThrow(name)
        return includeTemplates(item)
    }

    fun includeTemplates(item: Item): Item {
        var mergedItem = item
        for (templateName in item.includeTemplates) {
            val itemTemplate = getTemplateOrThrow(templateName)
            mergedItem = mergedItem.includeTemplate(itemTemplate)
        }
        return mergedItem
    }

    fun getItems(): Map<String, Item> = items.toMap()

    fun getItemsIncludeTemplates(): Map<String, Item> =
        items.mapValues { (_, value) -> includeTemplates(value) }

    fun listItems(): List<Item> = items.values.toList()

    fun listItemsIncludeTemplates(): List<Item> = getItemsIncludeTemplates().values.toList()
}