package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Access
import ru.scisolutions.scicmscore.util.Acl

interface AccessRepository : CrudRepository<Access, String> {
    @Query(value = Acl.ACCESS_SELECT_SNIPPET, nativeQuery = true)
    fun findAllByMask(mask: Set<Int>, username: String, roles: Set<String>): List<Access>
}