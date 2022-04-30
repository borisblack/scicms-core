package ru.scisolutions.scicmscore.engine.schema

import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.domain.model.ItemSpec

class ItemSpecValidator {
    fun validate(spec: ItemSpec) {
        for ((attrName, attribute) in spec.attributes) {
            if (attribute.type == Type.relation) {
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }
            }
        }
    }
}