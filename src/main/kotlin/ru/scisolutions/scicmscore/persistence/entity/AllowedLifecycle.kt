package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table

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
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var isDefault: Boolean = false
) : AbstractEntity()