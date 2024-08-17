package ru.scisolutions.scicmscore.engine.model.itemrec

import java.time.OffsetDateTime

open class ItemRec(
    private val map: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
) : MutableMap<String, Any?> by map {
    var id: String? by map
    var configId: String? by map
    var generation: Int? by map
    var majorRev: String? by map
    var minorRev: String? by map
    var current: Boolean? by map
    var locale: String? by map
    var lifecycle: String? by map
    var state: String? by map
    var permission: String? by map
    var createdAt: OffsetDateTime? by map
    var createdBy: String? by map
    var updatedAt: OffsetDateTime? by map
    var updatedBy: String? by map
    var lockedBy: String? by map

    fun asString(attrName: String): String =
        this[attrName]?.let { if (it is String) it else it.toString() }
            ?: throw IllegalArgumentException("Attribute [$attrName] is null.")

    companion object {
        const val ID_ATTR_NAME = "id"
        const val CONFIG_ID_ATTR_NAME = "configId"
        const val MAJOR_REV_ATTR_NAME = "majorRev"
        const val CURRENT_ATTR_NAME = "current"
        const val LOCALE_ATTR_NAME = "locale"
        const val STATE_ATTR_NAME = "state"
        const val CORE_ATTR_NAME = "core"

        const val LOCKED_BY_COL_NAME = "locked_by_id"
        const val PERMISSION_COL_NAME = "permission_id"

        const val PASSWORD_PLACEHOLDER = "********"
    }
}
