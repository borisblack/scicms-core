package ru.scisolutions.scicmscore.engine.schema.service

import ru.scisolutions.scicmscore.engine.schema.model.Schema

interface SchemaSeeder {
    fun seedSchema(schema: Schema)
}