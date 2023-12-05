package ru.scisolutions.scicmscore.persistence.repository

import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Access
import ru.scisolutions.scicmscore.util.Acl

interface AccessRepository : CrudRepository<Access, String> {
    @Query(Acl.ACCESS_JPQL_SNIPPET)
    @QueryHints(QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findAllByMask(mask: Set<Int>, username: String, roles: Set<String>): List<Access>
}