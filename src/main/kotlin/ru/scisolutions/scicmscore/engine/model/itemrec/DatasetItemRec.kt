package ru.scisolutions.scicmscore.engine.model.itemrec

class DatasetItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map.withDefault { null }) {
    var datasource: String? by this.map
}
