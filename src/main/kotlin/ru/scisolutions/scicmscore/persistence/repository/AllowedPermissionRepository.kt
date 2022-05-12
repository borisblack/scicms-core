package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission
import ru.scisolutions.scicmscore.util.ACL

interface AllowedPermissionRepository : CrudRepository<AllowedPermission, String> {
    @Query(
        value = "SELECT a.target_id FROM sec_allowed_permissions a WHERE a.source_id = :itemId",
        nativeQuery = true
    )
    fun findPermissionIdsByItemId(itemId: String): Set<String>

    @Query(
        value =
            "SELECT a.target_id FROM sec_allowed_permissions a " +
                "LEFT JOIN core_items i ON a.source_id = i.id " +
            "WHERE i.name = :itemName",
        nativeQuery = true
    )
    fun findPermissionIdsByItemName(itemName: String): Set<String>
}