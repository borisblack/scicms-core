package ru.scisolutions.scicmscore.engine.handler.util

import ru.scisolutions.scicmscore.engine.persistence.entity.Item

object DataHandlerUtil {
    fun prepareSelectedAttrNames(item: Item, selectAttrNames: Set<String>): Set<String> = selectAttrNames.asSequence()
        .filter {
            val attribute = item.spec.getAttribute(it)
            !attribute.isCollection()
        }
        // .plus(ItemRec.ID_ATTR_NAME)
        .plus(item.idAttribute)
        .toSet()

    fun checkRequiredAttributes(item: Item, attrNames: Set<String>) {
        item.spec.attributes
            .filterValues { it.required }
            .forEach { (attrName, _) ->
                if (attrName !in attrNames) {
                    throw IllegalArgumentException("Attribute [$attrName] is required")
                }
            }
    }
}
