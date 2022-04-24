package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Media

interface MediaRepository : CrudRepository<Media, String> {
    fun getById(id: String): Media
}