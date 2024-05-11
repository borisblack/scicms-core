package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.User

interface UserRepository : CrudRepository<User, String> {
    fun existsByUsername(username: String): Boolean
}