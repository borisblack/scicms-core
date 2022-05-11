package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Locale

interface LocaleRepository : CrudRepository<Locale, String> {
    fun existsByName(name: String): Boolean
}