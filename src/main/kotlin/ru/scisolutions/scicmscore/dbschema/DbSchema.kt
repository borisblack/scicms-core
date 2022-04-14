package ru.scisolutions.scicmscore.dbschema

import ru.scisolutions.scicmscore.api.model.AbstractModel
import ru.scisolutions.scicmscore.api.model.BaseMetadata
import ru.scisolutions.scicmscore.api.model.Item
import ru.scisolutions.scicmscore.api.model.ItemTemplate
import ru.scisolutions.scicmscore.api.model.Property
import ru.scisolutions.scicmscore.api.model.Spec

class DbSchema {
    private val itemTemplates = mutableMapOf<String, ItemTemplate>()
    private val items = mutableMapOf<String, Item>()

    fun addAll(models: List<AbstractModel>) {
        for (model in models) {
            add(model)
        }
    }

    private fun add(model: AbstractModel) {
        val metadata = model.metadata
        when(model) {
            is ItemTemplate -> {
                validateSpec(metadata, model.spec)
                itemTemplates[metadata.name] = model
            }
            is Item -> {
                validateSpec(metadata, model.spec)
                items[metadata.name] = model
            }
            else -> throw IllegalArgumentException("${model::class} model type is not supported")
        }
    }

    private fun validateSpec(metadata: BaseMetadata, spec: Spec) {
        for ((name, property) in spec.properties) {
            if (property.type !in validPropertyTypes)
                throw IllegalArgumentException("Property [${metadata.name}.$name]: Invalid type (${property.type})")

            if (property.relType != null && property.relType !in validPropertyRelTypes)
                throw IllegalArgumentException("Property [${metadata.name}.$name]: Invalid relation type (${property.relType})")
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

    companion object {
        val validPropertyTypes = Property.Type.values()
            .map { it.value }
            .toSet()

        val validPropertyRelTypes = Property.RelType.values()
            .map { it.value }
            .toSet()
    }
}