package ru.scisolutions.scicmscore.dbschema

import ru.scisolutions.scicmscore.dbschema.model.AbstractModel
import ru.scisolutions.scicmscore.dbschema.model.ItemModel
import ru.scisolutions.scicmscore.dbschema.model.ItemTemplateModel

class DbSchema {
    private val itemTemplateModels = mutableMapOf<String, ItemTemplateModel>()
    private val itemModels = mutableMapOf<String, ItemModel>()

    fun addAll(models: List<AbstractModel>) {
        for (itemModel in models) {
            add(itemModel)
        }
    }

    private fun add(model: AbstractModel) {
        when(model) {
            is ItemTemplateModel -> itemTemplateModels[model.metadata.name] = model
            is ItemModel -> itemModels[model.metadata.name] = model
            else -> throw IllegalArgumentException("${model::class} model type is not supported")
        }
    }

    fun getItemModels(): Map<String, ItemModel> =
        itemModels.mapValues { (_, value) ->
            includeTemplates(value)
        }

    fun getItemModel(name: String): ItemModel {
        val itemModel = itemModels[name] ?: throw IllegalStateException("Item $name not found")
        return includeTemplates(itemModel)
    }

    private fun includeTemplates(itemModel: ItemModel): ItemModel {
        var mergedItemModel = itemModel
        for (templateName in itemModel.includeTemplates) {
            val itemTemplateModel = itemTemplateModels[templateName] ?: throw IllegalStateException("Template $templateName not found")
            mergedItemModel = mergedItemModel.includeTemplate(itemTemplateModel)
        }
        return mergedItemModel
    }
}