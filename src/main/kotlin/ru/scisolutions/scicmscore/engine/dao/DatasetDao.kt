package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse
import ru.scisolutions.scicmscore.model.AggregateType
import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetDao {
    fun load(
        dataset: Dataset,
        start: String?,
        end: String?,
        aggregateType: AggregateType?,
        groupBy: String?
    ): List<Map<String, Any?>>

    fun load(dataset: Dataset, input: DatasetInput): DatasetResponse
}