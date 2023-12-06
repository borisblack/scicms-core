package ru.scisolutions.scicmscore.persistence.service

import jakarta.persistence.EntityManagerFactory
import org.springframework.stereotype.Service

@Service
class CacheService(
    private val emf: EntityManagerFactory
) {
    fun clearSchemaCaches(vararg classes: Class<*>) {
        val cache = emf.cache
        for (clazz in classes) {
            cache.evict(clazz)
        }
    }

    fun clearAllSchemaCaches() {
        val cache = emf.cache
        // cache.evict(Item::class.java)
        // cache.evict(ItemTemplate::class.java)
        cache.evictAll()
    }
}