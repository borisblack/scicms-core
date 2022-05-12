package ru.scisolutions.scicmscore.persistence.entity

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_allowed_lifecycles")
class AllowedLifecycle(
    @Column(name = "sort_order")
    var sortOrder: Int? = null,

    @Column(name = "source_id", nullable = false)
    var sourceId: String,

    @Column(name = "target_id", nullable = false)
    var targetId: String,

    @Column(name = "is_default", nullable = false, columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var isDefault: Boolean = false
) : AbstractEntity()