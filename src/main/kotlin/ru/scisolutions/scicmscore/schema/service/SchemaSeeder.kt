package ru.scisolutions.scicmscore.schema.service

import ru.scisolutions.scicmscore.schema.model.Schema

interface SchemaSeeder {
    fun seedSchema(schema: Schema)
}