package ru.scisolutions.scicmscore.engine.model.itemrec

open class ItemItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map.withDefault { null }) {
    var includeTemplates: List<String>? by this.map
    var core: Boolean? by this.map
    var name: String? by this.map
    var displayName: String? by this.map
    var pluralName: String? by this.map
    var displayPluralName: String? by this.map
    var datasource: String? by this.map
    var performDdl: Boolean? by this.map
    var tableName: String? by this.map
    var query: String? by this.map
    var cacheTtl: Int? by this.map
    var idAttribute: String? by this.map
    var titleAttribute: String? by this.map
    var defaultSortAttribute: String? by this.map
    var defaultSortOrder: String? by this.map
    var description: String? by this.map
    var icon: String? by this.map
    var readOnly: Boolean? by this.map
    var versioned: Boolean? by this.map
    var manualVersioning: Boolean? by this.map
    var localized: Boolean? by this.map
    var notLockable: Boolean? by this.map
    var implementation: String? by this.map
    var revisionPolicy: String? by this.map
    var spec: Any? by this.map
}
