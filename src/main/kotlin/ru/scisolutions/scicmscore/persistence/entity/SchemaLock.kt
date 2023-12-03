package ru.scisolutions.scicmscore.persistence.entity

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "core_schema_lock")
class SchemaLock(
    @Id
    var id: Int?,

    @Column(name = "locked_by")
    var lockedBy: String?,

    @Column(name = "lock_until")
    var lockUntil: LocalDateTime?
)
