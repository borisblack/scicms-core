package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateCache
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateService
import java.util.concurrent.TimeUnit

@Service
class ItemTemplateCacheImpl(
    dataProps: DataProps,
    private val itemTemplateService: ItemTemplateService
) : ItemTemplateCache {
    private val cache: Cache<String, ItemTemplate> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.itemTemplateCacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override operator fun get(name: String): ItemTemplate? {
        var itemTemplate = cache.getIfPresent(name)
        if (itemTemplate == null)
            itemTemplate = itemTemplateService.findByName(name)

        if (itemTemplate != null)
            cache.put(name, itemTemplate)

        return itemTemplate
    }

    override fun getOrThrow(name: String): ItemTemplate = get(name) ?: throw IllegalArgumentException("Item Template [$name] not found")

    override operator fun set(name: String, itemTemplate: ItemTemplate) {
        val savedItemTemplate = itemTemplateService.save(itemTemplate)
        cache.put(name, savedItemTemplate)
    }

    override fun delete(name: String) {
        itemTemplateService.deleteByName(name)
        cache.invalidate(name)
    }
}