package ru.scisolutions.scicmscore.engine.persistence.dao

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.persistence.mapper.DatasetRecMapper
import ru.scisolutions.scicmscore.engine.persistence.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.DatasetRec
import ru.scisolutions.scicmscore.engine.model.response.CacheStatistic
import ru.scisolutions.scicmscore.engine.service.DatasetCacheManager
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset

@Service
class DatasetDao(
    private val dsManager: DatasourceManager,
    private val datasetCacheManager: DatasetCacheManager
) {
    fun load(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): CacheStatistic<List<DatasetRec>> =
        datasetCacheManager.get(dataset, sql, paramSource) {
            logger.trace("Running load SQL: {}", sql)
            if (paramSource.parameterNames.isNotEmpty()) {
                logger.trace(
                    "Binding parameters: {}",
                    paramSource.parameterNames.joinToString { "$it = ${paramSource.getValue(it)}" }
                )
            }

            dsManager.template(dataset.ds).query(sql, paramSource, DatasetRecMapper())
        }

    fun count(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): CacheStatistic<Int> {
        val countSQL = "SELECT COUNT(*) FROM ($sql) t"

        return datasetCacheManager.get(dataset, sql, paramSource) {
            logger.trace("Running count SQL: {}", countSQL)
            if (paramSource.parameterNames.isNotEmpty()) {
                logger.trace(
                    "Binding parameters: {}",
                    paramSource.parameterNames.joinToString { "$it = ${paramSource.getValue(it)}" }
                )
            }

            dsManager.template(dataset.ds).queryForObject(countSQL, paramSource, Int::class.java) as Int
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasetDao::class.java)
    }
}