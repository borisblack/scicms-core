package ru.scisolutions.scicmscore.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore

data class ItemSpec(
    val attributes: Map<String, Attribute> = emptyMap(),
    val indexes: Map<String, Index> = emptyMap()
) {
    @JsonIgnore
    val columnNameToAttrNameMap: Map<String, String> =
        attributes
            .filter { (_, attribute) -> !attribute.isCollection() }
            .map { (attrName, attribute) -> (attribute.columnName ?: attrName).lowercase() to attrName }
            .toMap()

    fun getAttributeOrThrow(attrName: String): Attribute =
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

    fun validate() {
        for ((attrName, attribute) in attributes) {
            validateAttribute(attrName, attribute)
        }
    }

    private fun validateAttribute(attrName: String, attribute: Attribute) {
        if (attribute.type == Attribute.Type.relation) {
            requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }
            requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }

            if (attribute.inversedBy != null && attribute.mappedBy != null)
                throw IllegalArgumentException("The [$attrName] attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

            if (attribute.relType == Attribute.RelType.oneToMany) {
                requireNotNull(attribute.mappedBy) {
                    "The [$attrName] attribute does not have a mappedBy field, which is required for the oneToMany relationship"
                }
            }

            if (attribute.relType == Attribute.RelType.manyToMany) {
                requireNotNull(attribute.intermediate) {
                    "The [$attrName] attribute does not have an intermediate field, which is required for the manyToMany relationship"
                }

                if (attribute.inversedBy == null && attribute.mappedBy == null)
                    throw IllegalArgumentException("The [$attrName] attribute does not have an inversedBy or mappedBy field, which is required for the manyToMany relationship")
            }
        }
    }
}