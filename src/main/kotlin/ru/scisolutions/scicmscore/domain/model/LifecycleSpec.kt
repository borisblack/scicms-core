package ru.scisolutions.scicmscore.domain.model

data class LifecycleSpec(
    val states: Map<String, State> = emptyMap()
) {
    fun getStateOrThrow(state: String): State =
        states[state] ?: throw IllegalArgumentException("State [$state] not found")

    fun merge(other: LifecycleSpec) = LifecycleSpec(
        states = merge(this.states, other.states)
    )

    private fun <T> merge(source: Map<String, T>, target: Map<String, T>): Map<String, T> {
        val merged = target.toMutableMap()
        merged.putAll(source)

        return merged
    }
}