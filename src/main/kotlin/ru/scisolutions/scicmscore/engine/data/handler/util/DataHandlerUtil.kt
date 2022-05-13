package ru.scisolutions.scicmscore.engine.data.handler.util

import ru.scisolutions.scicmscore.persistence.entity.Item

object DataHandlerUtil {
    private const val ID_ATTR_NAME = "id"

    fun prepareSelectedAttrNames(item: Item, selectAttrNames: Set<String>): Set<String> =
        selectAttrNames.asSequence()
            .filter {
                val attribute = item.spec.getAttributeOrThrow(it)
                !attribute.isCollection()
            }
            .plus(ID_ATTR_NAME)
            .toSet()

    fun checkRequiredAttributes(item: Item, attrNames: Set<String>) {
        item.spec.attributes
            .filterValues { it.required }
            .forEach { (attrName, _) ->
                if (attrName !in attrNames)
                    throw IllegalArgumentException("Attribute [$attrName] is required")
            }
    }

    fun merge(source: Map<String, Any?>, target: Map<String, Any?>) =
        target.mapValues { (key, value) ->
            if (key in source) source[key] else value
        }
}