package ru.scisolutions.scicmscore.persistence.repository

import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.AllowedLifecycle

interface AllowedLifecycleRepository : CrudRepository<AllowedLifecycle, String> {
    @Query(
        value = "SELECT a.* FROM core_allowed_lifecycles a WHERE a.source_id = :itemId ORDER BY a.sort_order",
        nativeQuery = true
    )
    fun findAllByItemId(itemId: String): List<AllowedLifecycle>

    @Query(
        value =
            "SELECT a.* FROM core_allowed_lifecycles a " +
                "LEFT JOIN core_items i ON a.source_id = i.id " +
            "WHERE i.name = :itemName " +
            "ORDER BY a.sort_order",
        nativeQuery = true
    )
    @QueryHints(QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findAllByItemName(itemName: String): List<AllowedLifecycle>
}