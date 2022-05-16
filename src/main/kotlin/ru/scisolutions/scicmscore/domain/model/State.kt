package ru.scisolutions.scicmscore.domain.model

import java.awt.Point

data class State(
    val displayName: String,
    val transitions: Map<String, Transition> = emptyMap(),
    val point: Point = Point()
)