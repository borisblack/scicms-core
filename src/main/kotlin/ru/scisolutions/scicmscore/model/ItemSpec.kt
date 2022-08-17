package ru.scisolutions.scicmscore.model

import com.fasterxml.jackson.annotation.JsonIgnore

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
}