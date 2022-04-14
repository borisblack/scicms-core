package ru.scisolutions.scicmscore.dbschema.seeder

import ru.scisolutions.scicmscore.dbschema.DbSchema

interface DbSchemaSeeder {
    fun seed(dbSchema: DbSchema)
}