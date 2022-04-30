package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemService {
    val items: Map<String, Item>

    fun getItem(itemName: String): Item

    fun save(item: Item): Item

    fun delete(item: Item)
}