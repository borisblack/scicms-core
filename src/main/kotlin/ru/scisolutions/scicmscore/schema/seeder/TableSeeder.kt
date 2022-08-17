package ru.scisolutions.scicmscore.schema.seeder

import ru.scisolutions.scicmscore.schema.model.Item
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

interface TableSeeder {
    fun create(item: Item)

    fun update(item: Item, existingItemEntity: ItemEntity)

    fun delete(existingItemEntity: ItemEntity)
}