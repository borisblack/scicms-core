package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemService {
    fun findAll(): Iterable<Item>

    fun findByName(name: String): Item?

    fun getByName(name: String): Item

    fun getMedia(): Item

    fun getLocation(): Item

    fun findByNameForWrite(name: String): Item?

    fun canCreate(name: String): Boolean

    fun save(item: Item): Item

    fun delete(item: Item)
}