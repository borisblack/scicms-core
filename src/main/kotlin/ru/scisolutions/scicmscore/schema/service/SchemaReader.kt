package ru.scisolutions.scicmscore.schema.service

import ru.scisolutions.scicmscore.schema.model.Schema

interface SchemaReader {
    fun read(): Schema
}