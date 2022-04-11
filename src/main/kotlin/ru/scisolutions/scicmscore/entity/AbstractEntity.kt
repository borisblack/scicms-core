package ru.scisolutions.scicmscore.entity

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
    val id: String = UUID.randomUUID().toString()

    @Column(name = "config_id", nullable = false)
    val configId: String = id

    @Column
    val generation: Int? = null

    @Column(name = "major_rev")
    val majorRev: String? = null

    @Column(name = "minor_rev")
    val minorRev: String? = null

    @Column(name = "last_version")
    val lastVersion: Boolean? = null

    @Column(name = "is_current")
    val isCurrent: Boolean? = null

    @Column(name = "released")
    val released: Boolean? = null

    @Column(name = "lifecycle_id")
    val lifecycleId: String? = null

    @Column
    val state: String? = null

    @Column(name = "permission_id")
    var permissionId: String? = null

    @Column(name = "locale")
    val locale: String? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    @Column(name = "created_by_id", nullable = false)
    lateinit var createdById: String

    @Column(name = "updated_at")
    lateinit var updatedAt: LocalDateTime

    @Column(name = "updated_by_id")
    lateinit var updatedById: String

    @Column(name = "owned_by_id")
    val ownedById: String? = null

    @Column(name = "managed_by_id")
    val managedById: String? = null

    @Column(name = "locked_by_id")
    val lockedById: String? = null
}
