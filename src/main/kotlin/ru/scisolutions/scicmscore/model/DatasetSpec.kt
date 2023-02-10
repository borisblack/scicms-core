package ru.scisolutions.scicmscore.model

data class DatasetSpec(
    val columns: Map<String, Column> = emptyMap()
)