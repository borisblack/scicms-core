package ru.scisolutions.scicmscore.engine.persistence.repository

import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.Property

interface PropertyRepository : CrudRepository<Property, String> {
    @Query("select p from Property p")
    @QueryHints(QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "false"))
    override fun findAll(): Iterable<Property>

    fun deleteByName(name: String)
}
