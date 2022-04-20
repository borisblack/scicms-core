package ru.scisolutions.scicmscore.dbschema.seeder

import ru.scisolutions.scicmscore.domain.model.Item
import ru.scisolutions.scicmscore.entity.Item as ItemEntity

interface ItemSeeder {
    fun create(item: Item)

    fun update(item: Item, itemEntity: ItemEntity)

    fun delete(itemEntity: ItemEntity)
}