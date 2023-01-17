package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetService {
    fun findByNameForRead(name: String): Dataset?
}