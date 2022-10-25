package ru.scisolutions.scicmscore.model

data class DashItem(
    val name: String,
    val label: String,
    val metric: String,
    val location: String?,
    val temporal: String?
)