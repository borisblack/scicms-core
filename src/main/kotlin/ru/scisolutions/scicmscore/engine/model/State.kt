package ru.scisolutions.scicmscore.engine.model

data class State(
    val transitions: Set<String> = emptySet()
)