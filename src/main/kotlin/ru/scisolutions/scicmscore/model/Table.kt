package ru.scisolutions.scicmscore.model

data class Table(
    val name: String,
    val columns: Map<String, Column>
)