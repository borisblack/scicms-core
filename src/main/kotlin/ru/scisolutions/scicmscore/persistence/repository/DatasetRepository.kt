package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.util.Acl

interface DatasetRepository : CrudRepository<Dataset, String> {
    @Query(
        value = "SELECT * FROM core_datasets d WHERE d.name = :name AND (d.permission_id IS NULL OR d.permission_id IN (${Acl.PERMISSION_IDS_SELECT_SNIPPET}))",
        nativeQuery = true
    )
    fun findByNameWithACL(name: String, mask: Set<Int>, username: String, roles: Set<String>): Dataset?
}