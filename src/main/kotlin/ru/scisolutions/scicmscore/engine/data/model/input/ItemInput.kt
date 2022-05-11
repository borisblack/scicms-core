package ru.scisolutions.scicmscore.engine.data.model.input

class ItemInput(
    val data: Map<String, Any?>,
    val majorRev: String? = null,
    val locale: String? = null
)