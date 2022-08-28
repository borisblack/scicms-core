package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Location

interface LocationService {
    fun findById(id: String): Location?

    fun findByIdForRead(id: String): Location?

    fun findByIdForDelete(id: String): Location?

    fun getById(id: String): Location

    fun existsById(id: String): Boolean

    fun save(location: Location): Location

    fun delete(location: Location)
}