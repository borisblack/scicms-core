package ru.scisolutions.scicmscore.persistence.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate
import java.util.concurrent.TimeUnit

@Service
class ItemTemplateCache(
    dataProps: DataProps,
    private val itemTemplateService: ItemTemplateService
) {
    private val cache: Cache<String, ItemTemplate> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.itemCacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    operator fun get(name: String): ItemTemplate? {
        var itemTemplate = cache.getIfPresent(name)
        if (itemTemplate == null)
            itemTemplate = itemTemplateService.findByName(name)

        if (itemTemplate != null)
            cache.put(name, itemTemplate)

        return itemTemplate
    }

    fun getOrThrow(name: String): ItemTemplate = get(name) ?: throw IllegalArgumentException("Item Template [$name] not found")

    operator fun set(name: String, itemTemplate: ItemTemplate) {
        val savedItemTemplate = itemTemplateService.save(itemTemplate)
        cache.put(name, savedItemTemplate)
    }

    fun delete(name: String) {
        itemTemplateService.deleteByName(name)
        cache.invalidate(name)
    }
}