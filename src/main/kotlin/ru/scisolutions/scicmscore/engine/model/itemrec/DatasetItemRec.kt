package ru.scisolutions.scicmscore.engine.model.itemrec

class DatasetItemRec(map: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }) : ItemRec(map) {
    var datasource: String? by map
}
