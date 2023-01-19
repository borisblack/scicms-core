package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.model.AggregateType
import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetDao {
    fun findAll(
        dataset: Dataset,
        start: String?,
        end: String?,
        aggregateType: AggregateType?,
        groupBy: String?
    ): List<Map<String, Any?>>
}