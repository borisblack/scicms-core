package ru.scisolutions.scicmscore.schema.service

import ru.scisolutions.scicmscore.schema.model.DbSchema

interface SchemaSeeder {
    fun seedSchema(dbSchema: DbSchema)
}