package ru.scisolutions.scicmscore.engine.schema.model

data class ItemSpec(
    val attributes: Map<String, Attribute> = emptyMap(),
    val indexes: Map<String, Index> = emptyMap()
) {
    fun merge(other: ItemSpec) = ItemSpec(
        attributes = merge(this.attributes, other.attributes),
        indexes = merge(this.indexes, other.indexes)
    )

    private fun <T> merge(source: Map<String, T>, target: Map<String, T>): Map<String, T> {
        val merged = target.toMutableMap()
        merged.putAll(source)

        return merged
    }
}