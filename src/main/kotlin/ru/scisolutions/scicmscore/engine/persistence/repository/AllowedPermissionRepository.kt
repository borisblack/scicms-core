package ru.scisolutions.scicmscore.engine.persistence.repository

import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.AllowedPermission

interface AllowedPermissionRepository : CrudRepository<AllowedPermission, String> {
    @Query("select ap from AllowedPermission ap where ap.sourceId = :itemId order by ap.sortOrder")
    @QueryHints(QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findAllByItemId(itemId: String): List<AllowedPermission>

    @Query(
        "select ap from AllowedPermission ap " +
            "left join Item i on ap.sourceId = i.id " +
            "where i.name = :itemName " +
            "order by ap.sortOrder",
    )
    @QueryHints(QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findAllByItemName(itemName: String): List<AllowedPermission>
}
