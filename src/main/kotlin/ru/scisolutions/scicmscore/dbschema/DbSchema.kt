package ru.scisolutions.scicmscore.dbschema

import ru.scisolutions.scicmscore.api.model.AbstractModel
import ru.scisolutions.scicmscore.api.model.Item
import ru.scisolutions.scicmscore.api.model.ItemTemplate

class DbSchema {
    private val itemTemplates = mutableMapOf<String, ItemTemplate>()
    private val items = mutableMapOf<String, Item>()

    fun addAll(models: List<AbstractModel>) {
        for (model in models) {
            add(model)
        }
    }

    private fun add(model: AbstractModel) {
        when(model) {
            is ItemTemplate -> itemTemplates[model.metadata.name] = model
            is Item -> items[model.metadata.name] = model
            else -> throw IllegalArgumentException("${model::class} model type is not supported")
        }
    }

    fun getItems(): Map<String, Item> =
        items.mapValues { (_, value) ->
            includeTemplates(value)
        }

    fun getItem(name: String): Item {
        val itemModel = items[name] ?: throw IllegalStateException("Item $name not found")
        return includeTemplates(itemModel)
    }

    private fun includeTemplates(item: Item): Item {
        var mergedItem = item
        for (templateName in item.includeTemplates) {
            val itemTemplate = itemTemplates[templateName] ?: throw IllegalStateException("Template $templateName not found")
            mergedItem = mergedItem.includeTemplate(itemTemplate)
        }
        return mergedItem
    }
}