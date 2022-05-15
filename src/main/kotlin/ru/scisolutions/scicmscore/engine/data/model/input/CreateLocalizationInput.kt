package ru.scisolutions.scicmscore.engine.data.model.input

class CreateLocalizationInput(
    val id: String,
    val data: Map<String, Any?>,
    val locale: String,
    val copyCollectionRelations: Boolean? = null
)