package ru.scisolutions.scicmscore.engine.model.itemrec

open class ItemItemRec(private val map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map) {
    var includeTemplates: List<String>?
        get() = this[INCLUDE_TEMPLATES_ATTR_NAME] as List<String>?
        set(value) { this[INCLUDE_TEMPLATES_ATTR_NAME] = value }

    var core: Boolean?
        get() = this[CORE_ATTR_NAME] as Boolean?
        set(value) { this[CORE_ATTR_NAME] = value }

    var name: String?
        get() = this[NAME_ATTR_NAME] as String?
        set(value) { this[NAME_ATTR_NAME] = value }

    var displayName: String?
        get() = this[DISPLAY_NAME_ATTR_NAME] as String?
        set(value) { this[DISPLAY_NAME_ATTR_NAME] = value }

    var pluralName: String?
        get() = this[PLURAL_NAME_ATTR_NAME] as String?
        set(value) { this[PLURAL_NAME_ATTR_NAME] = value }

    var displayPluralName: String?
        get() = this[DISPLAY_PLURAL_NAME_ATTR_NAME] as String?
        set(value) { this[DISPLAY_PLURAL_NAME_ATTR_NAME] = value }

    var datasource: String?
        get() = this[DATA_SOURCE_ATTR_NAME] as String?
        set(value) { this[DATA_SOURCE_ATTR_NAME] = value }

    var performDdl: Boolean?
        get() = this[PERFORM_DDL_ATTR_NAME] as Boolean?
        set(value) { this[PERFORM_DDL_ATTR_NAME] = value }

    var tableName: String?
        get() = this[TABLE_NAME_ATTR_NAME] as String?
        set(value) { this[TABLE_NAME_ATTR_NAME] = value }

    var query: String?
        get() = this[QUERY_ATTR_NAME] as String?
        set(value) { this[QUERY_ATTR_NAME] = value }

    var cacheTtl: Int?
        get() = this[CACHE_TTL_ATTR_NAME] as Int?
        set(value) { this[CACHE_TTL_ATTR_NAME] = value }

    var idAttribute: String?
        get() = this[ID_ATTRIBUTE_ATTR_NAME] as String?
        set(value) { this[ID_ATTRIBUTE_ATTR_NAME] = value }

    var titleAttribute: String?
        get() = this[TITLE_ATTRIBUTE_ATTR_NAME] as String?
        set(value) { this[TITLE_ATTRIBUTE_ATTR_NAME] = value }

    var defaultSortAttribute: String?
        get() = this[DEFAULT_SORT_ATTRIBUTE_ATTR_NAME] as String?
        set(value) { this[DEFAULT_SORT_ATTRIBUTE_ATTR_NAME] = value }

    var defaultSortOrder: String?
        get() = this[DEFAULT_SORT_ORDER_ATTR_NAME] as String?
        set(value) { this[DEFAULT_SORT_ORDER_ATTR_NAME] = value }

    var description: String?
        get() = this[DESCRIPTION_ATTR_NAME] as String?
        set(value) { this[DESCRIPTION_ATTR_NAME] = value }

    var icon: String?
        get() = this[ICON_ATTR_NAME] as String?
        set(value) { this[ICON_ATTR_NAME] = value }

    var readOnly: Boolean?
        get() = this[READ_ONLY_ATTR_NAME] as Boolean?
        set(value) { this[READ_ONLY_ATTR_NAME] = value }

    var versioned: Boolean?
        get() = this[VERSIONED_ATTR_NAME] as Boolean?
        set(value) { this[VERSIONED_ATTR_NAME] = value }

    var manualVersioning: Boolean?
        get() = this[MANUAL_VERSIONING_ATTR_NAME] as Boolean?
        set(value) { this[MANUAL_VERSIONING_ATTR_NAME] = value }

    var localized: Boolean?
        get() = this[LOCALIZED_ATTR_NAME] as Boolean?
        set(value) { this[LOCALIZED_ATTR_NAME] = value }

    var notLockable: Boolean?
        get() = this[NOT_LOCKABLE_ATTR_NAME] as Boolean?
        set(value) { this[NOT_LOCKABLE_ATTR_NAME] = value }

    var implementation: String?
        get() = this[IMPLEMENTATION_ATTR_NAME] as String?
        set(value) { this[IMPLEMENTATION_ATTR_NAME] = value }

    var revisionPolicy: String?
        get() = this[REVISION_POLICY_ATTR_NAME] as String?
        set(value) { this[REVISION_POLICY_ATTR_NAME] = value }

    var spec: Any?
        get() = this[SPEC_ATTR_NAME] as Any?
        set(value) { this[SPEC_ATTR_NAME] = value }

    companion object {
        const val INCLUDE_TEMPLATES_ATTR_NAME = "includeTemplates"
        const val CORE_ATTR_NAME = "core"
        const val NAME_ATTR_NAME = "name"
        const val DISPLAY_NAME_ATTR_NAME = "displayName"
        const val PLURAL_NAME_ATTR_NAME = "pluralName"
        const val DISPLAY_PLURAL_NAME_ATTR_NAME = "displayPluralName"
        const val DATA_SOURCE_ATTR_NAME = "datasource"
        const val PERFORM_DDL_ATTR_NAME = "performDdl"
        const val TABLE_NAME_ATTR_NAME = "tableName"
        const val QUERY_ATTR_NAME = "query"
        const val CACHE_TTL_ATTR_NAME = "cacheTtl"
        const val ID_ATTRIBUTE_ATTR_NAME = "idAttribute"
        const val TITLE_ATTRIBUTE_ATTR_NAME = "titleAttribute"
        const val DEFAULT_SORT_ATTRIBUTE_ATTR_NAME = "defaultSortAttribute"
        const val DEFAULT_SORT_ORDER_ATTR_NAME = "defaultSortOrder"
        const val DESCRIPTION_ATTR_NAME = "description"
        const val ICON_ATTR_NAME = "icon"
        const val READ_ONLY_ATTR_NAME = "readOnly"
        const val VERSIONED_ATTR_NAME = "versioned"
        const val MANUAL_VERSIONING_ATTR_NAME = "manualVersioning"
        const val LOCALIZED_ATTR_NAME = "localized"
        const val NOT_LOCKABLE_ATTR_NAME = "notLockable"
        const val IMPLEMENTATION_ATTR_NAME = "implementation"
        const val REVISION_POLICY_ATTR_NAME = "revisionPolicy"
        const val SPEC_ATTR_NAME = "spec"
    }
}