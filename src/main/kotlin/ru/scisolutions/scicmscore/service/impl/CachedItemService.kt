package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.repository.ItemRepository
import ru.scisolutions.scicmscore.service.ItemService

@Service
class CachedItemService(
    private val itemRepository: ItemRepository
) : ItemService {
    val itemMap = mutableMapOf<String, Item>()
    private var allFetched = false

    override fun getAll(): Collection<Item> {
        if (!allFetched) {
            val items = itemRepository.findAll()
            for (item in items) {
                if (item.name !in itemMap)
                    itemMap[item.name] = item
            }
            allFetched = true
        }
        return itemMap.values
    }
}