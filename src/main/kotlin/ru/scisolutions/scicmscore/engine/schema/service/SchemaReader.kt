package ru.scisolutions.scicmscore.engine.schema.service

import ru.scisolutions.scicmscore.engine.schema.model.Schema

interface SchemaReader {
    fun read(): Schema
}