package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemService {
    fun findAll(): Iterable<Item>

    fun findByName(name: String): Item?

    fun findByNameForWrite(name: String): Item?

    fun findByIdForDelete(id: String): Item?

    fun canCreate(name: String): Boolean

    fun save(item: Item): Item

    fun deleteByName(name: String)
}