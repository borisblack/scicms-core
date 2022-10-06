package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate

interface ItemTemplateCache {
    operator fun get(name: String): ItemTemplate?

    fun getOrThrow(name: String): ItemTemplate

    operator fun set(name: String, itemTemplate: ItemTemplate)

    fun delete(name: String)
}