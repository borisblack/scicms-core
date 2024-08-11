package ru.scisolutions.scicmscore.api.graphql.type

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.model.Attribute.RelType
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService

@Component
class IncludeAttributePolicy(
    private val itemService: ItemService,
) {
    fun includeInObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private) {
            return false
        }

        // if (!item.versioned && (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME))
        //     return false

        // if (!item.localized && attrName == LOCALE_ATTR_NAME)
        //     return false

        return true
    }

    fun includeInFiltersInputObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private) {
            return false
        }

        if (!item.versioned && (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME)) {
            return false
        }

        if (!item.localized && attrName == LOCALE_ATTR_NAME) {
            return false
        }

        if (attribute.type == FieldType.media && itemService.getMedia().ds != item.ds) {
            return false
        }

        if (attribute.isCollection()) {
            val targetItem = itemService.getByName(requireNotNull(attribute.target))
            if (targetItem.ds != item.ds) {
                return false
            }

            if (attribute.relType == RelType.manyToMany) {
                val intermediateItem = itemService.getByName(requireNotNull(attribute.intermediate))
                if (intermediateItem.ds != item.ds) {
                    return false
                }
            }
        }

        return true
    }

    fun includeInInputObjectType(item: Item, attrName: String, attribute: Attribute): Boolean {
        if (attribute.private || attribute.readOnly) {
            return false
        }

        if (item.versioned) {
            if (!item.manualVersioning && (attrName == MAJOR_REV_ATTR_NAME/* || attrName == MINOR_REV_ATTR_NAME*/)) {
                // minor revision can be set for any versioned item
                return false
            }
        } else {
            if (attrName == MAJOR_REV_ATTR_NAME || attrName == MINOR_REV_ATTR_NAME) {
                return false
            }
        }

        if (!item.localized && attrName == LOCALE_ATTR_NAME) {
            return false
        }

        if (attrName == STATE_ATTR_NAME) {
            // use [promote] for state change
            return false
        }

        if (attribute.type == FieldType.sequence) {
            return false
        }

        return true
    }

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
        private const val MINOR_REV_ATTR_NAME = "minorRev"
        private const val LOCALE_ATTR_NAME = "locale"
        private const val STATE_ATTR_NAME = "state"
    }
}
