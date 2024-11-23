package ru.scisolutions.scicmscore.engine.model.itemrec

open class UserItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map.withDefault { null }) {
    var username: String? by this.map
    var password: String? by this.map
    var enabled: Boolean? by this.map
}
