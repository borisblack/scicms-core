package ru.scisolutions.scicmscore.api.model

data class Index(
    val columns: Set<String>,
    val unique: Boolean = false
)