package ru.scisolutions.scicmscore.engine.model.input

class CreateInput(
    val data: Map<String, Any?>,
    val majorRev: String? = null,
    val locale: String? = null
)
