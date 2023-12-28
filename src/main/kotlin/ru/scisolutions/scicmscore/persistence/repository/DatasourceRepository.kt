package ru.scisolutions.scicmscore.persistence.repository

import jakarta.persistence.QueryHint
import org.hibernate.jpa.HibernateHints
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Datasource

interface DatasourceRepository : CrudRepository<Datasource, String> {
    @Query("select d from Datasource d where d.id = :id and (d.permissionId is null or d.permissionId in :permissionIds)")
    @QueryHints(QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findByIdWithACL(id: String, permissionIds: Set<String>): Datasource?

    @Query("select d from Datasource d where d.name = :name and (d.permissionId is null or d.permissionId in :permissionIds)")
    @QueryHints(QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findByNameWithACL(name: String, permissionIds: Set<String>): Datasource?
}