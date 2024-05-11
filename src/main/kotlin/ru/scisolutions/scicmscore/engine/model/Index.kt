package ru.scisolutions.scicmscore.engine.model

data class Index(
    val columns: Set<String>,
    val unique: Boolean = false
)