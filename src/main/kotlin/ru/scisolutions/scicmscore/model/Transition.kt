package ru.scisolutions.scicmscore.model

import java.awt.Point

data class Transition(
    val displayName: String? = null,
    val points: List<Point> = emptyList()
)