package ru.scisolutions.scicmscore.engine.persistence.service

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.hibernate.Session
import org.hibernate.stat.Statistics
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.persistence.entity.Item

@Service
class CacheService(
    private val emf: EntityManagerFactory,
    private val entityManager: EntityManager,
    private val cacheManager: CacheManager,
    private val redissonClient: RedissonClient,
) {
    fun clearSchemaCaches(vararg classes: Class<*>) {
        val cache = emf.cache
        for (clazz in classes) {
            cache.evict(clazz)
        }
    }

    fun clearAllSchemaCaches() {
        val cache = emf.cache
        val hibernateCache = cache.unwrap(org.hibernate.Cache::class.java)
        // cache.evict(Item::class.java)
        // cache.evict(ItemTemplate::class.java)
        // cache.evictAll()
        hibernateCache.evictAllRegions()
        logger.debug("All schema cashes cleared.")
    }

    fun optimizeSchemaCaches(changedItem: Item) {
        if (!changedItem.core || changedItem.name in excludedItemNames) {
            return
        }

        clearAllSchemaCaches()
    }

    fun printStatistics() {
        printStatisticsFromEntityManager()
        printCachesFromCacheManager()
        printCachesFromRedisson()
    }

    private fun printStatisticsFromEntityManager() {
        val session: Session = entityManager.delegate as Session
        val statistics: Statistics = session.sessionFactory.statistics
        statistics.logSummary()
    }

    private fun printCachesFromCacheManager() {
        logger.info("Caches from cacheManager:")
        for (cacheName in cacheManager.cacheNames) {
            logger.info(" - $cacheName")
        }
    }

    private fun printCachesFromRedisson() {
        val keys = redissonClient.keys.keys
        logger.info("Caches from Redis:")
        keys.filter { !it.startsWith("redisson__") }
            .forEach {
                logger.info(" - $it")
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CacheService::class.java)
        private val excludedItemNames = setOf(Item.MEDIA_ITEM_NAME)
    }
}
