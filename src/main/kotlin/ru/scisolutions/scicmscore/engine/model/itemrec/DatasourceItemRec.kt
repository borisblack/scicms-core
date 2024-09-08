package ru.scisolutions.scicmscore.engine.model.itemrec

import ru.scisolutions.scicmscore.engine.model.DatasourceType

class DatasourceItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map.withDefault { null }) {
    var name: String? by this.map

    var sourceType: DatasourceType?
        get() = map[SOURCE_TYPE_KEY]?.let { if (it is String) DatasourceType.valueOf(it) else null }
        set(value) {
            map[SOURCE_TYPE_KEY] = value?.name
        }

    var connectionString: String? by this.map
    var username: String? by this.map
    var password: String? by this.map
    var maxPoolSize: Int? by this.map
    var minIdle: Int? by this.map
    var media: String? by this.map
    var params: Map<*, *>? by this.map

    companion object {
        private const val SOURCE_TYPE_KEY = "sourceType"
    }
}
