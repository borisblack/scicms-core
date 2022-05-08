package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.util.AccessUtil

interface PermissionRepository : CrudRepository<Permission, String> {
    fun getById(id: String): Permission

    @Query(
        value = AccessUtil.PERMISSION_IDS_SELECT_SNIPPET,
        nativeQuery = true
    )
    fun findIdsFor(mask: Set<Int>, username: String, roles: Set<String>): Set<String>

    @Query(
        value = "SELECT * FROM sec_permissions p WHERE p.id IN (${AccessUtil.PERMISSION_IDS_SELECT_SNIPPET})",
        nativeQuery = true
    )
    fun findAllFor(mask: Set<Int>, username: String, roles: Set<String>): List<Permission>
}