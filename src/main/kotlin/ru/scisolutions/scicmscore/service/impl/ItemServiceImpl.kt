package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.repository.ItemRepository
import ru.scisolutions.scicmscore.service.ItemService

@Service
class ItemServiceImpl(private val itemRepository: ItemRepository) : ItemService {
    override val items: Map<String, Item> by lazy { fetchAll() }

    private fun fetchAll(): Map<String, Item> {
        val itemList = itemRepository.findAll()
        return itemList.associateBy { it.name }
    }

    override fun save(item: Item): Item = itemRepository.save(item)
}