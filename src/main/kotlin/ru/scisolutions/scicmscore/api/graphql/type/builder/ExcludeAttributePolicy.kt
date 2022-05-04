package ru.scisolutions.scicmscore.api.graphql.type.builder

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.persistence.entity.Item

class ExcludeAttributePolicy {
    fun excludeFromObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private)
            return false

        // if (!item.versioned && (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME))
        //     return false

        // if (!item.localized && attrName == LOCALE_ATTR_NAME)
        //     return false

        return true
    }

    fun excludeFromFiltersInputObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private)
            return false

        if (!item.versioned && (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME))
            return false

        if (!item.localized && attrName == LOCALE_ATTR_NAME)
            return false

        return true
    }

    fun excludeFromInputObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.keyed || attribute.private)
            return false

        if (item.versioned) {
            if (!item.manualVersioning && (attrName == MAJOR_REV_ATTR_NAME/* || attrName == MINOR_REV_ATTR_NAME*/)) // minor revision can be set for any versioned item
                return false
        } else {
            if (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME)
                return false
        }

        if (!item.localized && attrName == LOCALE_ATTR_NAME)
            return false

        if (attrName == STATE_ATTR_NAME) // use [promote] for state change
            return false

        if (attrName == CREATED_AT_ATTR_NAME || attrName == CREATED_BY_ATTR_NAME || attrName == UPDATED_AT_ATTR_NAME || attrName == UPDATED_BY_ATTR_NAME)
            return false

        if (attribute.type == Type.sequence)
            return false

        return true
    }

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
        private const val MINOR_REV_ATTR_NAME = "minorRev"
        private const val LOCALE_ATTR_NAME = "locale"
        private const val STATE_ATTR_NAME = "state"
        private const val CREATED_AT_ATTR_NAME = "createdAt"
        private const val CREATED_BY_ATTR_NAME = "createdBy"
        private const val UPDATED_AT_ATTR_NAME = "updatedAt"
        private const val UPDATED_BY_ATTR_NAME = "updatedBy"
    }
}