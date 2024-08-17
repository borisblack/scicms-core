package ru.scisolutions.scicmscore.engine.model.itemrec

open class UserItemRec(map: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }) : ItemRec(map) {
    var username: String? by map
    var password: String? by map
    var enabled: Boolean? by map
}
