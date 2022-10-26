package ru.scisolutions.scicmscore.model

data class Dataset(
    val itemName: String,
    val label: String,
    val metric: String,
    val location: String?,
    val temporal: String?
)