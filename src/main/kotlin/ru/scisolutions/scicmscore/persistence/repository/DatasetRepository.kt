package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetRepository : CrudRepository<Dataset, String> {
    @Query("select d from Dataset d where d.name = :name and (d.permissionId is null or d.permissionId in :permissionIds)",)
    fun findByNameWithACL(name: String, permissionIds: Set<String>): Dataset?

    fun existsByDatasourceId(id: String): Boolean
}