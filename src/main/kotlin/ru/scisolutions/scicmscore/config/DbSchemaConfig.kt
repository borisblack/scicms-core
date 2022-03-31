package ru.scisolutions.scicmscore.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.scisolutions.scicmscore.dbschema.DbSchemaReader

@Configuration
class DbSchemaConfig(
    @Value("\${scicms-core.db-schema.read-on-init:true}")
    private val readOnInit: Boolean,
    @Value("\${scicms-core.db-schema.path}")
    private val dbSchemaPath: String
) {
    private val logger: Logger = LoggerFactory.getLogger(DbSchemaConfig::class.java)

    @Bean
    fun dbSchemaReader(): DbSchemaReader {
        val reader = DbSchemaReader()
        if (readOnInit) {
            logger.info("DB schema read flag enabled")
            reader.readDbSchema(dbSchemaPath)
        }

        return reader
    }
}