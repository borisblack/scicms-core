package ru.scisolutions.scicmscore.persistence.entity

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_locations")
class Location(
    @Column(name = "display_name")
    var displayName: String?,

    @Column(name = "latitude", nullable = false)
    val latitude: BigDecimal,

    @Column(name = "longitude", nullable = false)
    val longitude: BigDecimal,

    @Column(name = "sort_order")
    val sortOrder: Int?
) : AbstractEntity()