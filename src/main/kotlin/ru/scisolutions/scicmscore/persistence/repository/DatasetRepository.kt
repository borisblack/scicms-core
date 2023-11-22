package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetRepository : CrudRepository<Dataset, String> {
    @Query(
        value = "SELECT * FROM bi_datasets d WHERE d.name = :name AND (d.permission_id IS NULL OR d.permission_id IN :permissionIds)",
        nativeQuery = true
    )
    fun findByNameWithACL(name: String, permissionIds: Set<String>): Dataset?

    fun existsByDatasourceId(id: String): Boolean
}