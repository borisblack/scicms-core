package ru.scisolutions.scicmscore.model

data class State(
    val transitions: Set<String> = emptySet()
)