package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemCache {
    operator fun get(name: String): Item?

    fun getOrThrow(name: String): Item

    fun getMedia(): Item

    fun getLocation(): Item

    operator fun set(name: String, item: Item)

    fun delete(name: String)
}