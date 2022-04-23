package ru.scisolutions.scicmscore.engine.schema

import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.Item

interface SchemaEngine {
    fun addModel(model: AbstractModel)

    fun addModels(models: Collection<AbstractModel>)

    fun getItem(name: String): Item

    fun getItems(): Map<String, Item>
}