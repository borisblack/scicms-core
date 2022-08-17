package ru.scisolutions.scicmscore.schema.reader

import ru.scisolutions.scicmscore.schema.model.DbSchema

interface SchemaReader {
    fun read(): DbSchema
}