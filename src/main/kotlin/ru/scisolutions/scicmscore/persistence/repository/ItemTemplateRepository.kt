package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.util.Acl

interface ItemTemplateRepository : CrudRepository<ItemTemplate, String> {
    fun findByName(name: String): ItemTemplate?

    @Query(
        value = "SELECT * FROM core_item_templates t WHERE t.id = :id AND (t.permission_id IS NULL OR t.permission_id IN (${Acl.PERMISSION_IDS_SELECT_SNIPPET}))",
        nativeQuery = true
    )
    fun findByIdWithACL(id: String, mask: Set<Int>, username: String, roles: Set<String>): ItemTemplate?

    @Query(
        value = "SELECT * FROM core_item_templates t WHERE t.name = :name AND (t.permission_id IS NULL OR t.permission_id IN (${Acl.PERMISSION_IDS_SELECT_SNIPPET}))",
        nativeQuery = true
    )
    fun findByNameWithACL(name: String, mask: Set<Int>, username: String, roles: Set<String>): ItemTemplate?

    fun deleteByName(name: String)
}