package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.entity.User

interface UserService {
    fun findByUsername(username: String): User?
}