package ru.scisolutions.scicmscore.engine.model

open class ItemItemRec(private val map: MutableMap<String, Any?> = mutableMapOf()) : MutableMap<String, Any?> by map {
    var name: String?
        get() = this[NAME_ATTR_NAME] as String?
        set(value) { this[NAME_ATTR_NAME] = value }

    var performDdl: Boolean?
        get() = this[PERFORM_DDL_ATTR_NAME] as Boolean?
        set(value) { this[PERFORM_DDL_ATTR_NAME] = value }

    var dataSource: String?
        get() = this[DATA_SOURCE_ATTR_NAME] as String?
        set(value) { this[DATA_SOURCE_ATTR_NAME] = value }

    var tableName: String?
        get() = this[TABLE_NAME_ATTR_NAME] as String?
        set(value) { this[TABLE_NAME_ATTR_NAME] = value }

    companion object {
        const val NAME_ATTR_NAME = "name"
        const val PERFORM_DDL_ATTR_NAME = "performDdl"
        const val DATA_SOURCE_ATTR_NAME = "dataSource"
        const val TABLE_NAME_ATTR_NAME = "tableName"
    }
}