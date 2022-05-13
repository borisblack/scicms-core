package ru.scisolutions.scicmscore.engine.data.model.input

class CreateVersionInput(
    val id: String,
    val data: Map<String, Any?>,
    val majorRev: String? = null,
    val locale: String? = null
)