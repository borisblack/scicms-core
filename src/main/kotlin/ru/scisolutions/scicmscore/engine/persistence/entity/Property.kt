package ru.scisolutions.scicmscore.engine.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "core_properties")
@Cacheable
@org.hibernate.annotations.Cache(
    usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE
)
@org.hibernate.annotations.NaturalIdCache
class Property(
    @Column(nullable = false, unique = true)
    @org.hibernate.annotations.NaturalId
    var name: String,

    @Column(name = "property_type", nullable = false)
    var type: String,

    @Column(name = "property_value")
    var value: String?,

    @Column(name = "default_value")
    var defaultValue: String?
) : AbstractEntity()
