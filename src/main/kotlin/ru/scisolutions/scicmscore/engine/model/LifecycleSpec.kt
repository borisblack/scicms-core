package ru.scisolutions.scicmscore.engine.model

data class LifecycleSpec(
    val startEvent: State = State(),
    val states: Map<String, State> = emptyMap()
) {
    fun getStateOrThrow(state: String): State = states[state] ?: throw IllegalArgumentException("State [$state] not found")
}
