package ru.scisolutions.scicmscore.persistence.entity

import java.time.OffsetDateTime
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

class AuditEntityListener {
    @PrePersist
    fun touchForCreate(target: Any) {
        val entity = target as AbstractEntity
        val now = OffsetDateTime.now()
        entity.permissionId = Permission.DEFAULT_PERMISSION_ID
        entity.createdAt = now
        entity.createdById = User.ROOT_USER_ID
        entity.updatedAt = now
        entity.updatedById = User.ROOT_USER_ID
    }

    @PreUpdate
    fun touchForUpdate(target: Any) {
        val entity = target as AbstractEntity
        entity.updatedAt = OffsetDateTime.now()
        entity.updatedById = User.ROOT_USER_ID
    }
}