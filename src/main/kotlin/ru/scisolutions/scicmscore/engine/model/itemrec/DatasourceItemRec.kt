package ru.scisolutions.scicmscore.engine.model.itemrec

class DatasourceItemRec(map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map) {
    var connectionString: String?
        get() = this[CONNECTION_STRING_ATTR_NAME] as String?
        set(value) {
            this[CONNECTION_STRING_ATTR_NAME] = value
        }

    var username: String?
        get() = this[USERNAME_ATTR_NAME] as String?
        set(value) {
            this[USERNAME_ATTR_NAME] = value
        }

    var password: String?
        get() = this[PASSWORD_ATTR_NAME] as String?
        set(value) {
            this[PASSWORD_ATTR_NAME] = value
        }

    var maxPoolSize: Int?
        get() = this[MAX_POOL_SIZE_ATTR_NAME] as Int?
        set(value) {
            this[MAX_POOL_SIZE_ATTR_NAME] = value
        }

    var minIdle: Int?
        get() = this[MIN_IDLE_ATTR_NAME] as Int?
        set(value) {
            this[MIN_IDLE_ATTR_NAME] = value
        }

    var params: Map<*, *>?
        get() = this[PARAMS_ATTR_NAME] as Map<*, *>?
        set(value) {
            this[PARAMS_ATTR_NAME] = value
        }

    companion object {
        const val CONNECTION_STRING_ATTR_NAME = "connectionString"
        const val USERNAME_ATTR_NAME = "username"
        const val PASSWORD_ATTR_NAME = "password"
        const val MAX_POOL_SIZE_ATTR_NAME = "maxPoolSize"
        const val MIN_IDLE_ATTR_NAME = "minIdle"
        const val PARAMS_ATTR_NAME = "params"
    }
}
