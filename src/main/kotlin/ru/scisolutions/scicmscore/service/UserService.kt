package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.User

interface UserService {
    fun findByUsername(username: String): User?

    fun getByUsername(username: String): User
}