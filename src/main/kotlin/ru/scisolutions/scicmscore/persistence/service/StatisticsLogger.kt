package ru.scisolutions.scicmscore.persistence.service

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.hibernate.stat.Statistics
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class StatisticsLogger(
    private val entityManager: EntityManager,
    private val cacheManager: CacheManager,
    private val redissonClient: RedissonClient
) {
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
        val cacheNames = cacheManager.cacheNames
        logger.info("Caches from cacheManager:")
        for (cacheName in cacheNames) {
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
        private val logger = LoggerFactory.getLogger(StatisticsLogger::class.java)
    }
}