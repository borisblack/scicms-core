package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.Media

interface MediaService {
    fun getById(id: String): Media

    fun save(media: Media): Media
}