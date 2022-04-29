package ru.scisolutions.scicmscore.domain.model

data class Index(
    val columns: Set<String>,
    val unique: Boolean = false
)