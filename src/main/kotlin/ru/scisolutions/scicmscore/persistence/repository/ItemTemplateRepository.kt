package ru.scisolutions.scicmscore.persistence.repository

import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate

interface ItemTemplateRepository : CrudRepository<ItemTemplate, String> {
    @Query("select i from ItemTemplate i")
    @QueryHints(QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "false"))
    override fun findAll(): Iterable<ItemTemplate>

    @Query("select it from ItemTemplate it where it.id = :id and (it.permissionId is null or it.permissionId in :permissionIds)")
    fun findByIdWithACL(id: String, permissionIds: Set<String>): ItemTemplate?

    @Query("select it from ItemTemplate it where it.name = :name and (it.permissionId is null or it.permissionId in :permissionIds)")
    fun findByNameWithACL(name: String, permissionIds: Set<String>): ItemTemplate?

    fun deleteByName(name: String)
}