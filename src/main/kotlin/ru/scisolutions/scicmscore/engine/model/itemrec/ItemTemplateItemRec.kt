package ru.scisolutions.scicmscore.engine.model.itemrec

open class ItemTemplateItemRec(private val map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map) {
    var core: Boolean?
        get() = this[CORE_ATTR_NAME] as Boolean?
        set(value) { this[CORE_ATTR_NAME] = value }

    var name: String?
        get() = this[NAME_ATTR_NAME] as String?
        set(value) { this[NAME_ATTR_NAME] = value }

    var pluralName: String?
        get() = this[PLURAL_NAME_ATTR_NAME] as String?
        set(value) { this[PLURAL_NAME_ATTR_NAME] = value }

    var spec: Any?
        get() = this[SPEC_ATTR_NAME] as Any?
        set(value) { this[SPEC_ATTR_NAME] = value }

    companion object {
        const val CORE_ATTR_NAME = "core"
        const val NAME_ATTR_NAME = "name"
        const val PLURAL_NAME_ATTR_NAME = "pluralName"
        const val SPEC_ATTR_NAME = "spec"
    }
}