package ru.scisolutions.scicmscore.engine.schema.seeder

import ru.scisolutions.scicmscore.engine.schema.model.Schema

interface SchemaSeeder {
    fun seed(schema: Schema)
}