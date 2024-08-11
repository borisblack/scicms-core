package ru.scisolutions.scicmscore.engine.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "sec_identities")
class Identity(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var principal: Boolean = false,
) : AbstractEntity()
