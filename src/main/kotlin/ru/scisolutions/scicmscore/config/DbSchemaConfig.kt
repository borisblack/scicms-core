package ru.scisolutions.scicmscore.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import ru.scisolutions.scicmscore.dbschema.reader.DbSchemaReader
import ru.scisolutions.scicmscore.dbschema.seeder.DbSchemaSeeder

@Configuration
class DbSchemaConfig(
    @Value("\${scicms-core.db-schema.seed-on-init:true}")
    seedOnInit: Boolean,
    dbSchemaReader: DbSchemaReader,
    dbSchemaSeeder: DbSchemaSeeder
) {
    init {
        if (seedOnInit) {
            logger.info("DB schema seed flag enabled. Trying to seed")
            val dbSchema = dbSchemaReader.read()
            dbSchemaSeeder.seed(dbSchema)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DbSchemaConfig::class.java)
    }
}