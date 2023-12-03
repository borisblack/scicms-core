package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "core_locales")
class Locale(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name")
    var displayName: String?
) : AbstractEntity()