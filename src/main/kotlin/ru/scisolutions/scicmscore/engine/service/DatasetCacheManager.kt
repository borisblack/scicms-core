package ru.scisolutions.scicmscore.engine.service

import org.redisson.api.RMapCache
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.response.CacheStatistic
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import java.util.concurrent.TimeUnit
import kotlin.time.TimeSource

@Service
class DatasetCacheManager(
    private val dataProps: DataProps,
    private val redissonClient: RedissonClient
) {
    init {
        // Clear caches on start
        clearAll()
    }

    final fun clearAll() {
        val keys = redissonClient.keys.getKeysByPattern("$DATASET_QUERY_RESULTS_REGION:*")
        for (key in keys) {
            val cache: RMapCache<String, Any?> = redissonClient.getMapCache(key)
            cache.clear()
        }
    }

    fun <T> get(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource, loader: () -> T): CacheStatistic<T> {
        val cacheTtl: Int = dataset.cacheTtl ?: dataProps.datasetQueryResultEntryTtlMinutes
        val datasetCache = getDatasetCache(dataset.name)
        val fullSql = if (cacheTtl > 0) sqlWithParams(sql, paramSource) else null

        val timeSource = TimeSource.Monotonic
        val startMark = timeSource.markNow()
        if (fullSql != null) {
            if (fullSql in datasetCache) {
                logger.trace("Returning cached result for SQL: {}", fullSql)
                val res = datasetCache[fullSql] as T
                return CacheStatistic(
                    res,
                    startMark.elapsedNow().inWholeMilliseconds,
                    true
                )
            }
            logger.trace("Loading missed result for SQL: {}", fullSql)
        }

        val res = loader()

        if (fullSql != null && res != null && (res !is Collection<*> || res.size <= dataProps.maxCachedRecordsSize)) {
            datasetCache.fastPut(fullSql, res, cacheTtl.toLong(), TimeUnit.MINUTES)
        }

        return CacheStatistic(
            res,
            startMark.elapsedNow().inWholeMilliseconds,
            false
        )
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