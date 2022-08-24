package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
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
    fun findAllByItemName(itemName: String): List<AllowedLifecycle>
}