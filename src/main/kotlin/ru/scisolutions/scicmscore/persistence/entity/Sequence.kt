package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "core_sequences")
class Sequence(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name")
    var displayName: String?,

    var prefix: String?,
    var suffix: String?,

    @Column(name = "initial_value", nullable = false)
    var initialValue: Int = 0,

    @Column(name = "current_value")
    var currentValue: Int? = initialValue,

    @Column(nullable = false)
    var step: Int = 1,

    @Column(name = "pad_with")
    var padWith: Char?,

    @Column(name = "pad_to")
    var padTo: Int?,
) : AbstractEntity()