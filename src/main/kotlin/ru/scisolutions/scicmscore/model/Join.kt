package ru.scisolutions.scicmscore.model

data class Join(
    val field: String? = null,
    val mainTableField: String? = null,
    val op: String
)