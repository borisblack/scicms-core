package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
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

    fun load(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): List<Map<String, Any?>>

    fun count(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): Int
}