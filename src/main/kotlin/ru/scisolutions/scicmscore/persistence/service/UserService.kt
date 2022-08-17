package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.User

interface UserService {
    fun getCurrentUser(): User

    fun getByUsername(username: String): User
}