package ru.scisolutions.scicmscore.engine.schema.model

data class Index(
    val columns: Set<String>,
    val unique: Boolean = false
)