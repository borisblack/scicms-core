package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.repository.ItemRepository
import ru.scisolutions.scicmscore.service.ItemService

@Service
@Repository
@Transactional
class ItemServiceImpl(private val itemRepository: ItemRepository) : ItemService {
    override val items: MutableMap<String, Item> by lazy { fetchAll() }

    @Transactional(readOnly = true)
    fun fetchAll(): MutableMap<String, Item> {
        val itemList = itemRepository.findAll()
        return itemList.associateBy { it.name }.toMutableMap()
    }

    override fun save(item: Item): Item {
        val savedItem = itemRepository.save(item)
        this.items[item.name] = savedItem
        return savedItem
    }
}