package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.repository.ItemRepository
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.util.ACL.Mask
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class ItemServiceImpl(
    dataProps: DataProps,
    private val itemRepository: ItemRepository
) : ItemService {
    private val itemCache: Cache<String, Item> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.itemCacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun findAll(): Iterable<Item> {
        val itemList = itemRepository.findAll()
        itemList.forEach { itemCache.put(it.name, it) }

        return itemList
    }

    @Transactional(readOnly = true)
    override fun findByName(name: String): Item? {
        var item = itemCache.getIfPresent(name)
        if (item == null)
            item = itemRepository.findByName(name)

        if (item != null)
            itemCache.put(name, item)

        return item
    }

    @Transactional(readOnly = true)
    override fun getByName(name: String): Item = findByName(name) ?: throw IllegalArgumentException("Item [$name] not found")

    override fun getMedia(): Item = getByName(MEDIA_ITEM_NAME)

    override fun getLocation(): Item = getByName(LOCATION_ITEM_NAME)

    @Transactional(readOnly = true)
    override fun findByNameForWrite(name: String): Item? = findByNameWithACL(name, Mask.WRITE)

    override fun findByIdForDelete(id: String): Item? = findByIdWithACL(id, Mask.DELETE)

    @Transactional(readOnly = true)
    override fun canCreate(name: String): Boolean = findByNameWithACL(name, Mask.CREATE) != null

    private fun findByIdWithACL(id: String, accessMask: Mask): Item? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        return itemRepository.findByIdWithACL(id, accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
    }

    private fun findByNameWithACL(name: String, accessMask: Mask): Item? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        return itemRepository.findByNameWithACL(name, accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
    }

    override fun save(item: Item): Item {
        val savedItem = itemRepository.save(item)
        itemCache.put(item.name, savedItem)
        return savedItem
    }

    override fun delete(item: Item) {
        itemRepository.delete(item)
        itemCache.invalidate(item.name)
    }

    companion object {
        private const val MEDIA_ITEM_NAME = "media"
        private const val LOCATION_ITEM_NAME = "location"
    }
}