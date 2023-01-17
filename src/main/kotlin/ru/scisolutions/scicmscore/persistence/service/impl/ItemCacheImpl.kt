package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import ru.scisolutions.scicmscore.persistence.service.ItemService
import java.util.concurrent.TimeUnit

@Service
class ItemCacheImpl(
    dataProps: DataProps,
    private val itemService: ItemService
) : ItemCache {
    private val cache: Cache<String, Item> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.itemCacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override operator fun get(name: String): Item? {
        var item = cache.getIfPresent(name)
        if (item == null)
            item = itemService.findByName(name)

        if (item != null)
            cache.put(name, item)

        return item
    }

    override fun getOrThrow(name: String): Item = get(name) ?: throw IllegalArgumentException("Item [$name] not found")

    override fun getMedia(): Item = getOrThrow(MEDIA_ITEM_NAME)

    override operator fun set(name: String, item: Item) {
        val savedItem = itemService.save(item)
        cache.put(name, savedItem)
    }

    override fun delete(name: String) {
        itemService.deleteByName(name)
        cache.invalidate(name)
    }

    companion object {
        private const val MEDIA_ITEM_NAME = "media"
    }
}