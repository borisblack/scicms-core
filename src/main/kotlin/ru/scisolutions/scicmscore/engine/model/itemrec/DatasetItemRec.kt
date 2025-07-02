package ru.scisolutions.scicmscore.engine.model.itemrec

class DatasetItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map.withDefault { null }) {
    var name: String? by this.map
    var datasource: String? by this.map
}
