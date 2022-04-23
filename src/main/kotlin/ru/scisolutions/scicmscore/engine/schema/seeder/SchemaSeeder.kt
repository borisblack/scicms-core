package ru.scisolutions.scicmscore.engine.schema.seeder

import ru.scisolutions.scicmscore.engine.schema.model.Item

interface SchemaSeeder {
    fun seed(items: Map<String, Item>)
}