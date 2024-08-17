package ru.scisolutions.scicmscore.engine.model.itemrec

open class ItemTemplateItemRec(map: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }) : ItemRec(map) {
    var core: Boolean? by map
    var name: String? by map
    var pluralName: String? by map
    var spec: Any? by map
}
