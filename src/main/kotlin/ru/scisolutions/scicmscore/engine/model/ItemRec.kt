package ru.scisolutions.scicmscore.engine.model

import java.time.OffsetDateTime

open class ItemRec(private val map: MutableMap<String, Any?> = mutableMapOf()) : MutableMap<String, Any?> by map {
    var id: String?
        get() = this[ID_ATTR_NAME] as String?
        set(value) { this[ID_ATTR_NAME] = value }

    var configId: String?
        get() = this[CONFIG_ID_ATTR_NAME] as String?
        set(value) { this[CONFIG_ID_ATTR_NAME] = value }

    var generation: Int?
        get() = this[GENERATION_ATTR_NAME] as Int?
        set(value) { this[GENERATION_ATTR_NAME] = value }

    var majorRev: String?
        get() = this[MAJOR_REV_ATTR_NAME] as String?
        set(value) { this[MAJOR_REV_ATTR_NAME] = value }

    var minorRev: String?
        get() = this[MINOR_REV_ATTR_NAME] as String?
        set(value) { this[MINOR_REV_ATTR_NAME] = value }

    var current: Boolean?
        get() = this[CURRENT_ATTR_NAME] as Boolean?
        set(value) { this[CURRENT_ATTR_NAME] = value }

    var locale: String?
        get() = this[LOCALE_ATTR_NAME] as String?
        set(value) { this[LOCALE_ATTR_NAME] = value }

    var lifecycle: String?
        get() = this[LIFECYCLE_ATTR_NAME] as String?
        set(value) { this[LIFECYCLE_ATTR_NAME] = value }

    var state: String?
        get() = this[STATE_ATTR_NAME] as String?
        set(value) { this[STATE_ATTR_NAME] = value }

    var permission: String?
        get() = this[PERMISSION_ATTR_NAME] as String?
        set(value) { this[PERMISSION_ATTR_NAME] = value }

    var createdAt: OffsetDateTime?
        get() = this[CREATED_AT_ATTR_NAME] as OffsetDateTime?
        set(value) { this[CREATED_AT_ATTR_NAME] = value }

    var createdBy: String?
        get() = this[CREATED_BY_ATTR_NAME] as String?
        set(value) { this[CREATED_BY_ATTR_NAME] = value }

    var updatedAt: OffsetDateTime?
        get() = this[UPDATED_AT_ATTR_NAME] as OffsetDateTime?
        set(value) { this[UPDATED_AT_ATTR_NAME] = value }

    var updatedBy: String?
        get() = this[UPDATED_BY_ATTR_NAME] as String?
        set(value) { this[UPDATED_BY_ATTR_NAME] = value }

    var lockedBy: String?
        get() = this[LOCKED_BY_ATTR_NAME] as String?
        set(value) { this[LOCKED_BY_ATTR_NAME] = value }

    companion object {
        const val ID_ATTR_NAME = "id"
        const val CONFIG_ID_ATTR_NAME = "configId"
        const val GENERATION_ATTR_NAME = "generation"
        const val MAJOR_REV_ATTR_NAME = "majorRev"
        const val MINOR_REV_ATTR_NAME = "minorRev"
        const val CURRENT_ATTR_NAME = "current"
        const val LOCALE_ATTR_NAME = "locale"
        const val LIFECYCLE_ATTR_NAME = "lifecycle"
        const val STATE_ATTR_NAME = "state"
        const val PERMISSION_ATTR_NAME = "permission"
        const val CREATED_AT_ATTR_NAME = "createdAt"
        const val CREATED_BY_ATTR_NAME = "createdBy"
        const val UPDATED_AT_ATTR_NAME = "updatedAt"
        const val UPDATED_BY_ATTR_NAME = "updatedBy"
        const val LOCKED_BY_ATTR_NAME = "lockedBy"
        const val CORE_ATTR_NAME = "lockedBy"

        const val ID_COL_NAME = "id"
        const val LOCKED_BY_COL_NAME = "locked_by_id"
        const val PERMISSION_COL_NAME = "permission_id"

        const val PASSWORD_PLACEHOLDER = "********"
    }
}