package ru.scisolutions.scicmscore.engine.schema.reader

import ru.scisolutions.scicmscore.engine.schema.model.Schema

interface DbSchemaReader {
    fun read(): Schema
}