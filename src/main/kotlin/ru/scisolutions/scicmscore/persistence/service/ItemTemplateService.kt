package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate

interface ItemTemplateService {
    fun findAll(): Iterable<ItemTemplate>

    fun findByName(name: String): ItemTemplate?

    fun getByName(name: String): ItemTemplate

    fun findByNameForWrite(name: String): ItemTemplate?

    fun save(itemTemplate: ItemTemplate): ItemTemplate

    fun delete(itemTemplate: ItemTemplate)
}