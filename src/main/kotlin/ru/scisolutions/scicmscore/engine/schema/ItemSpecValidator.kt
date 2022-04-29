package ru.scisolutions.scicmscore.engine.schema

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.ItemSpec

class ItemSpecValidator {
    fun validate(spec: ItemSpec) {
        for ((attrName, attribute) in spec.attributes) {
            if (attribute.type !in validAttributeTypes)
                throw IllegalArgumentException("Attribute [$attrName] has invalid type (${attribute.type})")

            if (attribute.relType != null && attribute.relType !in validAttributeRelTypes)
                throw IllegalArgumentException("Attribute [$attrName] has invalid relation type (${attribute.relType})")

            if (attribute.type == Attribute.Type.RELATION.value) {
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }
            }
        }
    }

    companion object {
        val validAttributeTypes = Attribute.Type.values()
            .map { it.value }
            .toSet()

        val validAttributeRelTypes = Attribute.RelType.values()
            .map { it.value }
            .toSet()
    }
}