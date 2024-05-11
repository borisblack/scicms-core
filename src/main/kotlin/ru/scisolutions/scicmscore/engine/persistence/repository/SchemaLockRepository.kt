package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.SchemaLock
import java.time.LocalDateTime

interface SchemaLockRepository : CrudRepository<SchemaLock, String> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "update SchemaLock l " +
        "set l.lockedBy = :lockedBy, l.lockUntil = :lockUntil " +
        "where l.id = 1 and (l.lockedBy is null or l.lockedBy = :lockedBy or l.lockUntil < current_timestamp)"
    )
    fun lock(lockedBy: String, lockUntil: LocalDateTime): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "update SchemaLock l " +
        "set l.lockedBy = null, l.lockUntil = null " +
        "where l.id = 1 and (l.lockedBy is null or l.lockedBy = :lockedBy or l.lockUntil < current_timestamp)"
    )
    fun unlock(lockedBy: String): Int
}