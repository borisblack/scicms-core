package ru.scisolutions.scicmscore.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import ru.scisolutions.scicmscore.persistence.converter.MapConverter

@Entity
@Table(name = "sec_users")
class User(
    @Column(nullable = false)
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