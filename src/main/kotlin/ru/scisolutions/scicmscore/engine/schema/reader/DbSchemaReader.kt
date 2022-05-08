package ru.scisolutions.scicmscore.engine.schema.reader

import ru.scisolutions.scicmscore.engine.schema.model.DbSchema

interface DbSchemaReader {
    fun read(): DbSchema
}