package ru.scisolutions.scicmscore.engine.schema.seeder

import ru.scisolutions.scicmscore.engine.schema.model.Item

interface DbSchemaSeeder {
    fun seedSchema()

    fun seedItem(item: Item)
}