package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.util.AccessUtil
import ru.scisolutions.scicmscore.persistence.entity.Media

interface MediaRepository : CrudRepository<Media, String> {
    @Query(
        value = "SELECT * FROM core_media m WHERE m.id = :id AND m.permission_id IN (${AccessUtil.PERMISSION_IDS_SELECT_SNIPPET})",
        nativeQuery = true
    )
    fun findByIdWithACL(id: String, mask: Set<Int>, username: String, roles: Set<String>): Media?

    fun getById(id: String): Media
}