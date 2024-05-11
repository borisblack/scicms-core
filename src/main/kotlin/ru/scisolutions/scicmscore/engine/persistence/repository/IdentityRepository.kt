package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.Identity

interface IdentityRepository : CrudRepository<Identity, String> {
    fun findByNameAndPrincipal(name: String, principal: Boolean): Identity?

    fun deleteByNameAndPrincipal(name: String, principal: Boolean): Int
}