package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.ItemLock

interface ItemLockRepository : CrudRepository<ItemLock, String> {
    @Modifying
    @Query(
        "update ItemLock l " +
        "set l.locked = true, l.lockedAt = current_timestamp, l.lockedBy = :lockedBy " +
        "where l.id = 1 and (l.locked = false or (l.locked = true and l.lockedBy = :lockedBy))"
    )
    fun lock(lockedBy: String): Int

    @Modifying
    @Query(
        "update ItemLock l " +
        "set l.locked = false, l.lockedAt = null, l.lockedBy = null " +
        "where l.id = 1 and (l.locked = false or (l.locked = true and l.lockedBy = :lockedBy))"
    )
    fun unlock(lockedBy: String): Int
}