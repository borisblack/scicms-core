package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetDao {
    fun findAll(dataset: Dataset, start: String?, end: String?): List<Map<String, Any?>>
}