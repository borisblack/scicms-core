package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Location
import ru.scisolutions.scicmscore.util.Acl

interface LocationRepository : CrudRepository<Location, String> {
    @Query(
        value = "SELECT * FROM core_locations l WHERE l.id = :id AND (l.permission_id IS NULL OR l.permission_id IN (${Acl.PERMISSION_IDS_SELECT_SNIPPET}))",
        nativeQuery = true
    )
    fun findByIdWithACL(id: String, mask: Set<Int>, username: String, roles: Set<String>): Location?

    fun getById(id: String): Location
}