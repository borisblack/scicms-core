package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.util.Acl

interface PermissionRepository : CrudRepository<Permission, String> {
    @Query(
        value = Acl.PERMISSION_IDS_SELECT_SNIPPET,
        nativeQuery = true
    )
    fun findIdsFor(mask: Set<Int>, username: String, roles: Set<String>): Set<String>
}