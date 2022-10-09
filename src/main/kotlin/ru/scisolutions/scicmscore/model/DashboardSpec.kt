package ru.scisolutions.scicmscore.model

data class DashboardSpec(
    val rows: Int = 0,
    val cols: Int = 0,
    val dashes: List<Dash> = emptyList()
)