package ru.scisolutions.scicmscore.schema.service

import ru.scisolutions.scicmscore.schema.model.DbSchema

interface SchemaReader {
    fun read(): DbSchema
}