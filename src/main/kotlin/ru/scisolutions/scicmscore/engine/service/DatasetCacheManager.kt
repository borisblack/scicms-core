package ru.scisolutions.scicmscore.engine.service

import org.redisson.api.RMapCache
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import java.util.concurrent.TimeUnit

@Service
class DatasetCacheManager(
    private val dataProps: DataProps,
    private val redissonClient: RedissonClient
) {
    fun <T> get(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource, loader: () -> T): T {
        val fullSql = sqlWithParams(sql, paramSource)
        val datasetCache = getDatasetCache(dataset.name)
        if (fullSql in datasetCache) {
            logger.trace("Returning cached result for SQL: {}", fullSql)
            return datasetCache[fullSql] as T
        }

        logger.trace("Loading missed result for SQL: {}", fullSql)
        val res = loader()

        if (res != null && (res !is Collection<*> || res.size <= dataProps.maxCachedRecordsSize)) {
            datasetCache.fastPut(fullSql, res, dataProps.itemQueryResultEntryTtlMinutes, TimeUnit.MINUTES)
        }

        return res
    }

    private fun getDatasetCache(datasetName: String): RMapCache<String, Any?> {
        val datasetCache: RMapCache<String, Any?> = redissonClient.getMapCache("$DATASET_QUERY_RESULTS_REGION:$datasetName")
        if (datasetCache.isEmpty()) {
            datasetCache.setMaxSize(dataProps.datasetQueryResultMaxEntries)
        }

        return datasetCache
    }

    private fun sqlWithParams(sql: String, paramSource: DatasetSqlParameterSource): String {
        var resSql: String = sql
        for (paramName in paramSource.parameterNames) {
            resSql = resSql.replace(":$paramName", "'${paramSource.getValue(paramName)?.toString()}'")
        }

        return resSql
    }

    fun clear(dataset: Dataset) =
        getDatasetCache(dataset.name).clear()

    companion object {
        private const val DATASET_QUERY_RESULTS_REGION = "scicms_dataset_query_results"
        private val logger = LoggerFactory.getLogger(DatasetCacheManager::class.java)
    }
}