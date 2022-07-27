package ru.scisolutions.scicmscore.persistence.entity

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "core_items_lock")
class ItemLock(
    @Id
    var id: Int?,

    @Column(name = "locked", nullable = false, columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var locked: Boolean,

    @Column(name = "locked_at")
    var lockedAt: LocalDateTime?,

    @Column(name = "locked_by")
    var lockedBy: String?
)