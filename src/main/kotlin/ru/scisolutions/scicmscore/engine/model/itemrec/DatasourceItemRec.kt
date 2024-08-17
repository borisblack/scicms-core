package ru.scisolutions.scicmscore.engine.model.itemrec

class DatasourceItemRec(map: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }) : ItemRec(map) {
    var connectionString: String? by map
    var username: String? by map
    var password: String? by map
    var maxPoolSize: Int? by map
    var minIdle: Int? by map
    var params: Map<*, *>? by map
}
