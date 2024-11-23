package ru.scisolutions.scicmscore.engine.model.itemrec

open class ItemTemplateItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map.withDefault { null }) {
    var core: Boolean? by this.map
    var name: String? by this.map
    var pluralName: String? by this.map
    var spec: Any? by this.map
}
