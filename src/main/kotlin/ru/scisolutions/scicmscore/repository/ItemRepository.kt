package ru.scisolutions.scicmscore.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.entity.Item
import java.util.UUID

interface ItemRepository : CrudRepository<Item, UUID> {
    fun findAllByOrderByName(): Collection<Item>
}