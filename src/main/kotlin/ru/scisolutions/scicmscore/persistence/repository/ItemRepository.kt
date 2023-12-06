package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemRepository : CrudRepository<Item, String> {
    @Query("select i from Item i where i.id = :id and (i.permissionId is null or i.permissionId in :permissionIds)")
    fun findByIdWithACL(id: String, permissionIds: Set<String>): Item?

    @Query("select i from Item i where i.name = :name and (i.permissionId is null or i.permissionId in :permissionIds)")
    fun findByNameWithACL(name: String, permissionIds: Set<String>): Item?

    fun existsByDatasourceId(id: String): Boolean

    fun deleteByName(name: String)
}