package ru.scisolutions.scicmscore.engine.data.handler.impl

import ru.scisolutions.scicmscore.persistence.entity.Item

open class BaseHandler {
    protected fun prepareAttrNames(item: Item, selectAttrNames: Set<String>): Set<String> =
        selectAttrNames.asSequence()
            .filter {
                val attribute = item.spec.getAttributeOrThrow(it)
                !attribute.isCollection()
            }
            .plus(ID_ATTR_NAME)
            .toSet()

    companion object {
        const val ID_ATTR_NAME = "id"
    }
}