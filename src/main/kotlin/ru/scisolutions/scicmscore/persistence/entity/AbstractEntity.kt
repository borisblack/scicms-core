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

    var generation: Int? = null

    @Column(name = "major_rev")
    var majorRev: String? = null

    @Column(name = "minor_rev")
    var minorRev: String? = null

    @Column(name = "last_version")
    var lastVersion: Boolean? = null

    @Column(name = "is_current")
    var isCurrent: Boolean? = null

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
