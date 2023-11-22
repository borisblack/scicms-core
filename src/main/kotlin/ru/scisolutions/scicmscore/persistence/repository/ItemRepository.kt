package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemRepository : CrudRepository<Item, String> {
    fun findByName(name: String): Item?

    @Query(
        value = "SELECT * FROM core_items i WHERE i.id = :id AND (i.permission_id IS NULL OR i.permission_id IN :permissionIds)",
        nativeQuery = true
    )
    fun findByIdWithACL(id: String, permissionIds: Set<String>): Item?

    @Query(
        value = "SELECT * FROM core_items i WHERE i.name = :name AND (i.permission_id IS NULL OR i.permission_id IN :permissionIds)",
        nativeQuery = true
    )
    fun findByNameWithACL(name: String, permissionIds: Set<String>): Item?

    fun existsByDatasourceId(id: String): Boolean

    fun deleteByName(name: String)
}