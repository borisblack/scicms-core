package ru.scisolutions.scicmscore.persistence.entity

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_locations")
class Location(
    @Column(nullable = false)
    val latitude: BigDecimal,

    @Column(nullable = false)
    val longitude: BigDecimal,

    @Column(nullable = false)
    var label: String?,

    @Column(name = "sort_order")
    val sortOrder: Int?
) : AbstractEntity()