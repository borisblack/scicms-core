package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.util.AccessUtil

interface ItemRepository : CrudRepository<Item, String> {
    fun findByName(name: String): Item?

    @Query(
        value = "SELECT * FROM core_items i WHERE i.id = :id AND i.permission_id IN (${AccessUtil.PERMISSION_IDS_SELECT_SNIPPET})",
        nativeQuery = true
    )
    fun findByIdWithACL(id: String, mask: Set<Int>, username: String, roles: Set<String>): Item?

    @Query(
        value = "SELECT * FROM core_items i WHERE i.name = :name AND i.permission_id IN (${AccessUtil.PERMISSION_IDS_SELECT_SNIPPET})",
        nativeQuery = true
    )
    fun findByNameWithACL(name: String, mask: Set<Int>, username: String, roles: Set<String>): Item?
}