package ru.scisolutions.scicmscore.engine.model

data class Join(
    val field: String? = null,
    val mainTableField: String? = null,
    val op: String,
)
