package ru.scisolutions.scicmscore.engine.model.itemrec

open class UserItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map.withDefault { null }) {
    var username: String? by map
    var password: String? by map
    var enabled: Boolean? by map
}
