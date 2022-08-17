package ru.scisolutions.scicmscore.model

import ru.scisolutions.scicmscore.util.Maps

data class LifecycleSpec(
    val states: Map<String, State> = emptyMap()
) {
    fun getStateOrThrow(state: String): State =
        states[state] ?: throw IllegalArgumentException("State [$state] not found")

    fun merge(other: LifecycleSpec) = LifecycleSpec(
        states = Maps.merge(this.states, other.states)
    )
}