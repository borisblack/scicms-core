package ru.scisolutions.scicmscore.model

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.scisolutions.scicmscore.util.Maps

data class ItemSpec(
    val attributes: Map<String, Attribute> = emptyMap(),
    val indexes: Map<String, Index> = emptyMap()
) {
    @JsonIgnore
    val columnNameToAttrNameMap: Map<String, String> =
        attributes
            .filter { (_, attribute) -> !attribute.isCollection() }
            .map { (attrName, attribute) -> (attribute.columnName ?: attrName.lowercase()) to attrName }
            .toMap()

    fun getAttributeOrThrow(attrName: String): Attribute =
        attributes[attrName] ?: throw IllegalArgumentException("Attribute [$attrName] not found")

    fun merge(other: ItemSpec) = ItemSpec(
        attributes = Maps.merge(this.attributes, other.attributes),
        indexes = Maps.merge(this.indexes, other.indexes)
    )
}