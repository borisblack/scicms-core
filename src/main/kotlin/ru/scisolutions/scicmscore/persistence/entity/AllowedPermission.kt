package ru.scisolutions.scicmscore.persistence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "sec_allowed_permissions")
class AllowedPermission(
    @Column(name = "sort_order")
    var sortOrder: Int? = null,

    @Column(name = "source_id", nullable = false)
    var sourceId: String,

    @Column(name = "target_id", nullable = false)
    var targetId: String,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false
) : AbstractEntity()