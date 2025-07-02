package ru.scisolutions.scicmscore.engine.persistence.service

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.hibernate.Session
import org.hibernate.stat.Statistics
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.itemrec.DatasetItemRec
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.service.DatasetCacheManager
import ru.scisolutions.scicmscore.engine.service.ItemCacheManager

@Service
class CacheService(
    private val emf: EntityManagerFactory,
    private val entityManager: EntityManager,
    private val cacheManager: CacheManager,
    private val itemCacheManager: ItemCacheManager,
    private val datasetCacheManager: DatasetCacheManager,
    private val redissonClient: RedissonClient
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

    /**
     * Clears the appropriate caches when the item's record changes.
     */
    fun actualizeCaches(item: Item, itemRec: ItemRec) {
        if (!item.core || isItemExcluded(item.name)) {
            return
        }

        clearAllSchemaCaches()

        if (isSecurityItem(item.name)) {
            // Security settings have been changed. Clear item and dataset caches
            itemCacheManager.clearAll()
            datasetCacheManager.clearAll()
        } else if (item.name == Item.DATASET_ITEM_NAME) {
            // In dataset RLS could have changed. Clear dataset cache
            val datasetItemRec = DatasetItemRec(itemRec)
            datasetCacheManager.clear(requireNotNull(datasetItemRec.name))
        }
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

    private fun isItemExcluded(itemName: String) = itemName in excludedItemNames

    private fun isSecurityItem(itemName: String) = itemName in securityItemNames

    companion object {
        private val logger = LoggerFactory.getLogger(CacheService::class.java)
        private val excludedItemNames = setOf(Item.MEDIA_ITEM_NAME)
        private val securityItemNames = setOf(
            Item.ACCESS_ITEM_NAME,
            Item.ALLOWED_PERMISSION_ITEM_NAME,
            Item.GROUP_ITEM_NAME,
            Item.GROUP_MEMBER_ITEM_NAME,
            Item.GROUP_ROLE_ITEM_NAME,
            Item.IDENTITY_ITEM_NAME,
            Item.PERMISSION_ITEM_NAME,
            Item.ROLE_ITEM_NAME,
            Item.USER_ITEM_NAME
        )
    }
}
