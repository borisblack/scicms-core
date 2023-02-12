package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.persistence.entity.Dataset

interface DatasetDao {
    fun load(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): List<Map<String, Any?>>

    fun count(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): Int

    fun actualizeSpec(dataset: Dataset)
}