package ru.scisolutions.scicmscore.engine.persistence.repository

import jakarta.persistence.QueryHint
import org.hibernate.jpa.HibernateHints
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset

interface DatasetRepository : CrudRepository<Dataset, String> {
    @Query("select d from Dataset d where d.name = :name and (d.permissionId is null or d.permissionId in :permissionIds)")
    @QueryHints(QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findByNameWithACL(name: String, permissionIds: Set<String>): Dataset?

    fun existsByDatasourceId(id: String): Boolean
}
