package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.Media

interface MediaService {
    fun findByIdForRead(id: String): Media?

    fun findById(id: String): Media?

    fun getById(id: String): Media

    fun save(media: Media): Media
}