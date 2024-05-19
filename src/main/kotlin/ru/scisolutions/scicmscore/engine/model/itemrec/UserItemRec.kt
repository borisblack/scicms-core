package ru.scisolutions.scicmscore.engine.model.itemrec

open class UserItemRec(private val map: MutableMap<String, Any?> = mutableMapOf()) : ItemRec(map) {
    var username: String?
        get() = this[USERNAME_ATTR_NAME] as String?
        set(value) { this[USERNAME_ATTR_NAME] = value }

    var password: String?
        get() = this[PASSWORD_ATTR_NAME] as String?
        set(value) { this[PASSWORD_ATTR_NAME] = value }

    var enabled: Boolean?
        get() = this[ENABLED_ATTR_NAME] as Boolean?
        set(value) { this[ENABLED_ATTR_NAME] = value }

    companion object {
        const val USERNAME_ATTR_NAME = "username"
        const val PASSWORD_ATTR_NAME = "password"
        const val ENABLED_ATTR_NAME = "enabled"
    }
}