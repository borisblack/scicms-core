package ru.scisolutions.scicmscore.dbschema.seeder

import ru.scisolutions.scicmscore.api.model.Item
import ru.scisolutions.scicmscore.entity.Item as ItemEntity

interface ItemSeeder {
    fun seed(item: Item, itemEntity: ItemEntity?)
}