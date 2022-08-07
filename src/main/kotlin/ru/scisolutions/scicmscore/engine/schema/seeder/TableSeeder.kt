package ru.scisolutions.scicmscore.engine.schema.seeder

import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

interface TableSeeder {
    fun create(item: Item)

    fun update(item: Item, existingItemEntity: ItemEntity)

    fun delete(itemEntity: ItemEntity)
}