package ru.scisolutions.scicmscore.schema.model

class DbSchema {
    private val itemTemplates = mutableMapOf<String, ItemTemplate>()
    private val items = mutableMapOf<String, Item>()

    fun putModels(models: Collection<AbstractModel>) = models.forEach { putModel(it) }

    fun putModel(model: AbstractModel) {
        validateModel(model)

        val metadata = model.metadata
        when(model) {
            is ItemTemplate -> {
                if (metadata.name in itemTemplates)
                    throw IllegalArgumentException("Model [${metadata.name}] already exists in schema")

                itemTemplates[metadata.name] = model
            }
            is Item -> {
                if (metadata.name in items)
                    throw IllegalArgumentException("Model [${metadata.name}] already exists in schema")

                items[metadata.name] = model
            }
            else -> throw IllegalArgumentException("${model::class.simpleName} model type is not supported")
        }
    }

    private fun validateModel(model: AbstractModel) {
        if(model.metadata.name.first().isUpperCase())
            throw IllegalArgumentException("Model name [${model.metadata.name}] must start with a lowercase character")
    }

    fun getItemTemplates(): Map<String, ItemTemplate> = itemTemplates.toMap()

    fun getItems(): Map<String, Item> = items.toMap()
}