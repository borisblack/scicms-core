package ru.scisolutions.scicmscore.persistence.service

import jakarta.persistence.EntityManagerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate

@Service
class CacheService(
    private val emf: EntityManagerFactory
) {
    fun clearSchemaCaches() {
        val cache = emf.cache
        cache.evict(Item::class.java)
        cache.evict(ItemTemplate::class.java)
        cache.evictAll()
    }
}