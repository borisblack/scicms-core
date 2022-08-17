package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Media

interface MediaService {
    fun findById(id: String): Media?

    fun findByIdForRead(id: String): Media?

    fun findByIdForDelete(id: String): Media?

    fun getById(id: String): Media

    fun existsById(id: String): Boolean

    fun save(media: Media): Media
}