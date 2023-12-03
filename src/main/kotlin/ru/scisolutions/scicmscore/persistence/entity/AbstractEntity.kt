package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.time.OffsetDateTime
import java.util.UUID

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

    @Column(name = "is_current", nullable = false, columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var isCurrent: Boolean = true

    @Column(name = "lifecycle_id")
    var lifecycleId: String? = null

    var state: String? = null

    @Column(name = "permission_id")
    var permissionId: String? = null

    var locale: String? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: OffsetDateTime

    @Column(name = "created_by_id", nullable = false)
    lateinit var createdById: String

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null

    @Column(name = "updated_by_id")
    var updatedById: String? = null

    @Column(name = "locked_by_id")
    var lockedById: String? = null
}
