package ru.scisolutions.scicmscore.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type

data class ItemSpec(
    val attributes: Map<String, Attribute> = emptyMap(),
    val indexes: Map<String, Index> = emptyMap()
) {
    @JsonIgnore
    val columnNameToAttrNameMap: Map<String, String> =
        attributes
            .filter { (_, attribute) ->
                attribute.type != Type.RELATION.value || (attribute.relType != RelType.ONE_TO_MANY.value && attribute.relType != RelType.MANY_TO_MANY.value)
            }
            .map { (attrName, attribute) -> (attribute.columnName ?: attrName).lowercase() to attrName }
            .toMap()

    fun getAttribute(attrName: String): Attribute =
        attributes[attrName] ?: throw IllegalArgumentException("Attribute [$attrName] not found")

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