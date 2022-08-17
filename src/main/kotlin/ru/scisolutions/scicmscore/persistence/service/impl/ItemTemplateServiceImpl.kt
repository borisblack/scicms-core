package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.persistence.repository.ItemTemplateRepository
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class ItemTemplateServiceImpl(
    dataProps: DataProps,
    private val itemTemplateRepository: ItemTemplateRepository
) : ItemTemplateService {
    private val itemTemplateCache: Cache<String, ItemTemplate> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.itemTemplateCacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun findAll(): Iterable<ItemTemplate> {
        val itemTemplateList = itemTemplateRepository.findAll()
        itemTemplateList.forEach { itemTemplateCache.put(it.name, it) }

        return itemTemplateList
    }

    @Transactional(readOnly = true)
    override fun findByName(name: String): ItemTemplate? {
        var itemTemplate = itemTemplateCache.getIfPresent(name)
        if (itemTemplate == null)
            itemTemplate = itemTemplateRepository.findByName(name)

        if (itemTemplate != null)
            itemTemplateCache.put(name, itemTemplate)

        return itemTemplate
    }

    @Transactional(readOnly = true)
    override fun getByName(name: String): ItemTemplate = findByName(name) ?: throw IllegalArgumentException("Item Template [$name] not found")

    override fun save(itemTemplate: ItemTemplate): ItemTemplate {
        val savedItemTemplate = itemTemplateRepository.save(itemTemplate)
        itemTemplateCache.put(itemTemplate.name, savedItemTemplate)
        return savedItemTemplate
    }

    override fun delete(itemTemplate: ItemTemplate) {
        itemTemplateRepository.delete(itemTemplate)
        itemTemplateCache.invalidate(itemTemplate.name)
    }
}