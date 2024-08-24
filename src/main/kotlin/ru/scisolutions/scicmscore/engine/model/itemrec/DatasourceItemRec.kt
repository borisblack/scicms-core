package ru.scisolutions.scicmscore.engine.model.itemrec

class DatasourceItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map.withDefault { null }) {
    var connectionString: String? by map
    var username: String? by map
    var password: String? by map
    var maxPoolSize: Int? by map
    var minIdle: Int? by map
    var isFile: Boolean? by map
    var media: String? by map
    var params: Map<*, *>? by map
}
