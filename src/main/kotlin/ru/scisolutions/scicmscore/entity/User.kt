package ru.scisolutions.scicmscore.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "sec_users")
class User(
    @Column(nullable = false)
    val username: String,

//    @Column(nullable = false)
//    val password: String,

    @Column(nullable = false)
    val enabled: Boolean
) : AbstractEntity() {
    companion object {
        const val ROOT_USER_ID: String = "0c924266-3c61-4362-81d7-9d69403fbe32"
    }
}