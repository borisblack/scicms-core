package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetService {
    fun getById(id: String): Dataset

    fun findByNameForRead(name: String): Dataset?

    fun actualizeSpec(dataset: Dataset)
}