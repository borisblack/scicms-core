package ru.scisolutions.scicmscore.dbschema.reader

import ru.scisolutions.scicmscore.dbschema.DbSchema

interface DbSchemaReader {
    fun read(): DbSchema
}