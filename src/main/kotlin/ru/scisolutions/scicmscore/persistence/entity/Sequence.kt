package ru.scisolutions.scicmscore.persistence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

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
    var initialValue: Int,

    @Column(name = "current_value")
    var currentValue: Int? = initialValue,

    @Column(nullable = false)
    var step: Int,

    @Column(name = "pad_with")
    var padWith: Char?,

    @Column(name = "pad_to")
    var padTo: Int?,
) : AbstractEntity()