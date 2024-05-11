package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.Locale

interface LocaleRepository : CrudRepository<Locale, String> {
    fun existsByName(name: String): Boolean
}