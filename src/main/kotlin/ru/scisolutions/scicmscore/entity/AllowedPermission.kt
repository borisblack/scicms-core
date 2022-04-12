package ru.scisolutions.scicmscore.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "sec_allowed_permissions")
class AllowedPermission(
    @Column(name = "sort_order")
    val sortOrder: Int? = null,

    @Column(name = "source_id", nullable = false)
    val sourceId: String,

    @Column(name = "target_id", nullable = false)
    val targetId: String,

    @Column(name = "is_default", nullable = false)
    val isDefault: Boolean = false
) : AbstractEntity()