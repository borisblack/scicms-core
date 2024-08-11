package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.Media

interface MediaRepository : CrudRepository<Media, String> {
    @Query("select m from Media m where m.id = :id and (m.permissionId is null or m.permissionId in :permissionIds)")
    fun findByIdWithACL(id: String, permissionIds: Set<String>): Media?
}
