package ru.scisolutions.scicmscore.graphql.type.builder

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.entity.Item

class ExcludeAttributePolicy {
    fun excludeFromObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private)
            return false

        if (!item.versioned && (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME))
            return false

        if (!item.localized && attrName == LOCALE_ATTR_NAME)
            return false

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

        if (!item.versioned && (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME))
            return false

        if (!item.localized && attrName == LOCALE_ATTR_NAME)
            return false

        if (attrName == STATE_ATTR_NAME) // promote is used to change state
            return false

        return true
    }

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
        private const val MINOR_REV_ATTR_NAME = "minorRev"
        private const val LOCALE_ATTR_NAME = "locale"
        private const val STATE_ATTR_NAME = "state"
    }
}