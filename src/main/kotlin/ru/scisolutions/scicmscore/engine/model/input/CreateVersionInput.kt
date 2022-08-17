package ru.scisolutions.scicmscore.engine.model.input

class CreateVersionInput(
    val id: String,
    val data: Map<String, Any?>,
    val majorRev: String? = null,
    val locale: String? = null,
    val copyCollectionRelations: Boolean? = null
)