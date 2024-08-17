package ru.scisolutions.scicmscore.engine.model.itemrec

open class ItemItemRec(map: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }) : ItemRec(map) {
    var includeTemplates: List<String>? by map
    var core: Boolean? by map
    var name: String? by map
    var displayName: String? by map
    var pluralName: String? by map
    var displayPluralName: String? by map
    var datasource: String? by map
    var performDdl: Boolean? by map
    var tableName: String? by map
    var query: String? by map
    var cacheTtl: Int? by map
    var idAttribute: String? by map
    var titleAttribute: String? by map
    var defaultSortAttribute: String? by map
    var defaultSortOrder: String? by map
    var description: String? by map
    var icon: String? by map
    var readOnly: Boolean? by map
    var versioned: Boolean? by map
    var manualVersioning: Boolean? by map
    var localized: Boolean? by map
    var notLockable: Boolean? by map
    var implementation: String? by map
    var revisionPolicy: String? by map
    var spec: Any? by map
}
