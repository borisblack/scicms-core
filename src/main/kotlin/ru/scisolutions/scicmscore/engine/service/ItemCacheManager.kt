package ru.scisolutions.scicmscore.engine.service

import org.redisson.api.RMapCache
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.db.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.persistence.entity.Item
import java.util.concurrent.TimeUnit

@Service
class ItemCacheManager(
    private val dataProps: DataProps,
    private val redissonClient: RedissonClient
) {
    init {
        // Clear caches on start
        val keys = redissonClient.keys.getKeysByPattern("$ITEM_QUERY_RESULTS_REGION:*")
        for (key in keys) {
            val cache: RMapCache<String, Any?> = redissonClient.getMapCache(key)
            cache.clear()
        }
    }

    fun <T> get(item: Item, sql: String, paramSource: AttributeSqlParameterSource, loader: () -> T): T {
        val cacheTtl: Int = item.cacheTtl ?: dataProps.itemQueryResultEntryTtlMinutes
        val itemCache = getItemCache(item.name)
        val fullSql = sqlWithParams(sql, paramSource)
        if (cacheTtl > 0) {
            if (fullSql in itemCache) {
                logger.trace("Returning cached result for SQL: {}", fullSql)
                return itemCache[fullSql] as T
            }
            logger.trace("Loading missed result for SQL: {}", fullSql)
        }

        val res = loader()

        if (cacheTtl > 0 && res != null && (res !is Collection<*> || res.size <= dataProps.maxCachedRecordsSize)) {
            itemCache.fastPut(fullSql, res, cacheTtl.toLong(), TimeUnit.MINUTES)
        }

        return res
    }

    private fun getItemCache(itemName: String): RMapCache<String, Any?> {
        val itemCache: RMapCache<String, Any?> = redissonClient.getMapCache("$ITEM_QUERY_RESULTS_REGION:$itemName")
        if (itemCache.isEmpty()) {
            itemCache.setMaxSize(dataProps.itemQueryResultMaxEntries)
        }

        return itemCache
    }

    private fun sqlWithParams(sql: String, paramSource: AttributeSqlParameterSource): String {
        var resSql: String = sql
        for (paramName in paramSource.parameterNames) {
            resSql = resSql.replace(":$paramName", "'${paramSource.getValue(paramName)?.toString()}'")
        }

        return resSql
    }

    fun clear(item: Item) =
        getItemCache(item.name).clear()

    companion object {
        private const val ITEM_QUERY_RESULTS_REGION = "scicms_item_query_results"
        private val logger = LoggerFactory.getLogger(ItemCacheManager::class.java)
    }
}