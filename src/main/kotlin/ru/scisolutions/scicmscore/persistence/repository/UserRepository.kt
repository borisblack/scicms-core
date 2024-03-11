package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.User

interface UserRepository : CrudRepository<User, String> {
    fun existsByUsername(username: String): Boolean
}