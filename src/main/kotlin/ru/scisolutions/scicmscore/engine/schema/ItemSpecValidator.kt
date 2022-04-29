package ru.scisolutions.scicmscore.engine.schema

import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.domain.model.ItemSpec

class ItemSpecValidator {
    fun validate(spec: ItemSpec) {
        for ((attrName, attribute) in spec.attributes) {
            val attrType = Type.valueOf(attribute.type)

            if (attribute.relType != null)
                RelType.valueOf(attribute.relType)

            if (attrType == Type.relation) {
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }
            }
        }
    }
}