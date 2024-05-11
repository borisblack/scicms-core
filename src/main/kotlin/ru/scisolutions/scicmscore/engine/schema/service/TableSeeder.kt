package ru.scisolutions.scicmscore.engine.schema.service

import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.persistence.entity.Item as ItemEntity

interface TableSeeder {
    fun create(item: Item)

    fun update(item: Item, existingItemEntity: ItemEntity)

    fun delete(existingItemEntity: ItemEntity)

    fun dropTable(dataSource: String, tableName: String)
}