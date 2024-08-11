package ru.scisolutions.scicmscore.engine.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "core_schema_lock")
class SchemaLock(
    @Id
    var id: Int?,
    @Column(name = "locked_by")
    var lockedBy: String?,
    @Column(name = "lock_until")
    var lockUntil: LocalDateTime?,
)
