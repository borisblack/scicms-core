package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.Sequence

interface SequenceRepository : CrudRepository<Sequence, String> {
    fun existsByName(name: String): Boolean

    fun getByName(name: String): Sequence

    @Modifying
    @Query("update Sequence s set s.currentValue = :nextValue where s.name = :name and s.currentValue is null")
    fun initCurrentValueByName(name: String, nextValue: Int): Int

    @Modifying
    @Query("update Sequence s set s.currentValue = :nextValue where s.name = :name and s.currentValue = :currentValue")
    fun updateCurrentValueByName(name: String, currentValue: Int, nextValue: Int): Int
}
