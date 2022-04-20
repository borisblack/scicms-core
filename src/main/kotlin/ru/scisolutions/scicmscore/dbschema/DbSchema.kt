package ru.scisolutions.scicmscore.dbschema

import ru.scisolutions.scicmscore.domain.model.AbstractModel
import ru.scisolutions.scicmscore.domain.model.BaseMetadata
import ru.scisolutions.scicmscore.domain.model.Item
import ru.scisolutions.scicmscore.domain.model.ItemTemplate
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.ItemSpec

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

    private fun validateSpec(metadata: BaseMetadata, spec: ItemSpec) {
        for ((name, attribute) in spec.attributes) {
            if (attribute.type !in validAttributeTypes)
                throw IllegalArgumentException("Attribute [${metadata.name}.$name]: Invalid type (${attribute.type})")

            if (attribute.relType != null && attribute.relType !in validAttributeRelTypes)
                throw IllegalArgumentException("Attribute [${metadata.name}.$name]: Invalid relation type (${attribute.relType})")
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
        val validAttributeTypes = Attribute.Type.values()
            .map { it.value }
            .toSet()

        val validAttributeRelTypes = Attribute.RelType.values()
            .map { it.value }
            .toSet()
    }
}