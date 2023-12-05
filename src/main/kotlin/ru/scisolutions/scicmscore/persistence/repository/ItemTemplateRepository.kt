package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate

interface ItemTemplateRepository : CrudRepository<ItemTemplate, String> {
    @Query(
        value = "SELECT * FROM core_item_templates t WHERE t.id = :id AND (t.permission_id IS NULL OR t.permission_id IN :permissionIds)",
        nativeQuery = true
    )
    fun findByIdWithACL(id: String, permissionIds: Set<String>): ItemTemplate?

    @Query(
        value = "SELECT * FROM core_item_templates t WHERE t.name = :name AND (t.permission_id IS NULL OR t.permission_id IN :permissionIds)",
        nativeQuery = true
    )
    fun findByNameWithACL(name: String, permissionIds: Set<String>): ItemTemplate?

    fun deleteByName(name: String)
}