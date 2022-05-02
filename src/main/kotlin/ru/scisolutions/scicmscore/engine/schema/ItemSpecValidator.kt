package ru.scisolutions.scicmscore.engine.schema

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.domain.model.ItemSpec

class ItemSpecValidator {
    fun validate(spec: ItemSpec) {
        for ((attrName, attribute) in spec.attributes) {
            validateAttribute(attrName, attribute)
        }
    }

    private fun validateAttribute(attrName: String, attribute: Attribute) {
        if (attribute.type == Type.relation) {
            requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }
            requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }

            if (attribute.inversedBy != null && attribute.mappedBy != null)
                throw IllegalArgumentException("The [$attrName] attribute has both inversedBy and mappedBy fields, which is an invalid relation state")

            if (attribute.relType == RelType.oneToMany) {
                requireNotNull(attribute.mappedBy) {
                    "The [$attrName] attribute does not have a mappedBy field, which is required for the oneToMany relationship"
                }
            }

            if (attribute.relType == RelType.manyToMany) {
                requireNotNull(attribute.intermediate) {
                    "The [$attrName] attribute does not have an intermediate field, which is required for the manyToMany relationship"
                }

                if (attribute.inversedBy == null && attribute.mappedBy == null)
                    throw IllegalArgumentException("The [$attrName] attribute does not have an inversedBy or mappedBy field, which is required for the manyToMany relationship")
            }
        }
    }
}