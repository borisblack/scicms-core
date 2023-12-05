package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import ru.scisolutions.scicmscore.persistence.converter.MapConverter

@Entity
@Table(name = "sec_users")
@Cacheable
@org.hibernate.annotations.Cache(
    usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE
)
@org.hibernate.annotations.NaturalIdCache
class User(
    @Column(nullable = false, unique = true)
    @org.hibernate.annotations.NaturalId
    var username: String,

   // @Column(name = "passwd", nullable = false)
   // val password: String,

    @Column(nullable = false, columnDefinition = "TINYINT")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter::class)
    var enabled: Boolean,

    @Convert(converter = MapConverter::class)
    var sessionData: Map<String, Any?>? = null,
) : AbstractEntity() {
    companion object {
        const val ROOT_USER_ID: String = "0c924266-3c61-4362-81d7-9d69403fbe32"
    }
}