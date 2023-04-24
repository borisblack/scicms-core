package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.User

interface UserCache {
    operator fun get(username: String): User?

    fun getOrThrow(username: String): User

    fun getCurrent(): User

    fun save(user: User): User
}