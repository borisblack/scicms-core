package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.entity.Item

interface ItemService {
    val items: Map<String, Item>
    fun save(item: Item): Item
}