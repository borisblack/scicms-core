package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.entity.Item

interface ItemService {
    fun getAll(): Collection<Item>
}