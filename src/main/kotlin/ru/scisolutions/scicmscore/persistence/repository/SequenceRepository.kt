package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Sequence

interface SequenceRepository : CrudRepository<Sequence, String> {
    fun getByName(name: String): Sequence

    @Modifying
    @Query("update Sequence s set s.currentValue = :nextValue where s.name = :name and s.currentValue = :currentValue")
    fun updateCurrentValueByName(name: String, currentValue: Int?, nextValue: Int): Int
}