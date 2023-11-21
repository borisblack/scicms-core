package ru.scisolutions.scicmscore.api.graphql.type

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.Attribute.RelType
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import ru.scisolutions.scicmscore.util.Schema

@Component
class IncludeAttributePolicy(
    private val itemCache: ItemCache
) {
    fun includeInObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private)
            return false

        // if (!item.versioned && (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME))
        //     return false

        // if (!item.localized && attrName == LOCALE_ATTR_NAME)
        //     return false

        return true
    }

    fun includeInFiltersInputObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private)
            return false

        if (!item.versioned && (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME))
            return false

        if (!item.localized && attrName == LOCALE_ATTR_NAME)
            return false

        if (attribute.type == FieldType.media && !Schema.areDataSourcesEqual(itemCache.getMedia().datasource?.name, item.datasource?.name))
            return false

        if (attribute.isCollection()) {
            val targetItem = itemCache.getOrThrow(requireNotNull(attribute.target))
            if (!Schema.areDataSourcesEqual(targetItem.datasource?.name, item.datasource?.name))
                return false

            if (attribute.relType == RelType.manyToMany) {
                val intermediateItem = itemCache.getOrThrow(requireNotNull(attribute.intermediate))
                if (!Schema.areDataSourcesEqual(intermediateItem.datasource?.name, item.datasource?.name))
                    return false
            }
        }

        return true
    }

    fun includeInInputObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private || attribute.readOnly)
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

        if (attribute.type == FieldType.sequence)
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