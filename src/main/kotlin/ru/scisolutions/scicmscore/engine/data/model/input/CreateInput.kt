package ru.scisolutions.scicmscore.engine.data.model.input

class CreateInput(
    val data: Map<String, Any?>,
    val majorRev: String? = null,
    val locale: String? = null
)