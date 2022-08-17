package ru.scisolutions.scicmscore.model

data class Index(
    val columns: Set<String>,
    val unique: Boolean = false
)