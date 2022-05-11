package ru.scisolutions.scicmscore.engine.schema.reader

import ru.scisolutions.scicmscore.engine.schema.model.DbSchema

interface SchemaReader {
    fun read(): DbSchema
}