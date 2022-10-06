package ru.scisolutions.scicmscore.persistence.entity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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
