package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.AllowedLifecycle

interface AllowedLifecycleRepository : CrudRepository<AllowedLifecycle, String> {
    @Query(
        value = "SELECT a.target_id FROM core_allowed_lifecycles a WHERE a.source_id = :itemId",
        nativeQuery = true
    )
    fun findLifecycleIdsByItemId(itemId: String): Set<String>

    @Query(
        value =
            "SELECT a.target_id FROM core_allowed_lifecycles a " +
                "LEFT JOIN core_items i ON a.source_id = i.id " +
            "WHERE i.name = :itemName",
        nativeQuery = true
    )
    fun findLifecycleIdsByItemName(itemName: String): Set<String>
}