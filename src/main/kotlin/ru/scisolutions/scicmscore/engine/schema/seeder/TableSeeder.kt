package ru.scisolutions.scicmscore.engine.schema.seeder

import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

interface TableSeeder {
    fun create(item: Item)

    fun update(item: Item, itemEntity: ItemEntity)

    fun delete(itemEntity: ItemEntity)
}