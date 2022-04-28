package ru.scisolutions.scicmscore.persistence.entity

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditEntityListener::class)
abstract class AbstractEntity {
    @Id
    var id: String = UUID.randomUUID().toString()

    @Column(name = "config_id", nullable = false)
    var configId: String = id

    @Column(nullable = false)
    var generation: Int = 1

    @Column(name = "major_rev", nullable = false)
    var majorRev: String = "A"

    @Column(name = "minor_rev")
    var minorRev: String? = null

    @Column(name = "last_version", nullable = false)
    var lastVersion: Boolean = true

    @Column(name = "is_current", nullable = false)
    var isCurrent: Boolean = true

    var released: Boolean? = null

    @Column(name = "lifecycle_id")
    var lifecycleId: String? = null

    var state: String? = null

    @Column(name = "permission_id")
    var permissionId: String? = null

    var locale: String? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    @Column(name = "created_by_id", nullable = false)
    lateinit var createdById: String

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @Column(name = "updated_by_id")
    var updatedById: String? = null

    @Column(name = "owned_by_id")
    var ownedById: String? = null

    @Column(name = "managed_by_id")
    var managedById: String? = null

    @Column(name = "locked_by_id")
    var lockedById: String? = null
}
