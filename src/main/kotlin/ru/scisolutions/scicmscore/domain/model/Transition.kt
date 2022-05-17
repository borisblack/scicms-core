package ru.scisolutions.scicmscore.domain.model

import java.awt.Point

data class Transition(
    val displayName: String? = null,
    val points: List<Point> = emptyList()
)