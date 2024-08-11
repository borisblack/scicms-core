package ru.scisolutions.scicmscore.engine.persistence.repository

import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.AllowedLifecycle

interface AllowedLifecycleRepository : CrudRepository<AllowedLifecycle, String> {
    @Query("select al from AllowedLifecycle al where al.sourceId = :itemId order by al.sortOrder")
    @QueryHints(QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findAllByItemId(itemId: String): List<AllowedLifecycle>

    @Query(
        "select al from AllowedLifecycle al " +
            "left join Item i on al.sourceId = i.id " +
            "where i.name = :itemName " +
            "order by al.sortOrder",
    )
    @QueryHints(QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findAllByItemName(itemName: String): List<AllowedLifecycle>
}
