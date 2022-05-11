package ru.scisolutions.scicmscore.persistence.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "core_locales")
class Locale(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name")
    var displayName: String?
) : AbstractEntity()