package ru.scisolutions.scicmscore.api.model

data class Spec(
    val properties: Map<String, Property> = emptyMap(),
    val indexes: Map<String, Index> = emptyMap()
) {
    fun merge(other: Spec) = Spec(
        properties = merge(this.properties, other.properties),
        indexes = merge(this.indexes, other.indexes)
    )

    private fun <T> merge(source: Map<String, T>, target: Map<String, T>): Map<String, T> {
        val merged = target.toMutableMap()
        merged.putAll(source)

        return merged
    }
}