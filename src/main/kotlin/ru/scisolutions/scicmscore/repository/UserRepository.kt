package ru.scisolutions.scicmscore.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.entity.User

interface UserRepository : CrudRepository<User, String> {
    fun findByUsername(username: String): User?
}