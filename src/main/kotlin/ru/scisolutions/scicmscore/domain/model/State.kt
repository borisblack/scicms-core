package ru.scisolutions.scicmscore.domain.model

import java.awt.Point

data class State(
    val displayName: String,
    val transitions: Set<String> = emptySet(),
    val point: Point = Point()
)