package ru.scisolutions.scicmscore.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.scisolutions.scicmscore.dbschema.DbSchemaReader
import ru.scisolutions.scicmscore.dbschema.DbSchemaSeeder
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.service.PermissionService

@Configuration
class DbSchemaConfig(
    @Value("\${scicms-core.db-schema.path}")
    private val dbSchemaPath: String,
    @Value("\${scicms-core.db-schema.seed-on-init:true}")
    private val seedOnInit: Boolean,
    private val itemService: ItemService,
    private val permissionService: PermissionService,
) {
    private val logger: Logger = LoggerFactory.getLogger(DbSchemaConfig::class.java)

    @Bean
    fun dbSchemaSeeder(): DbSchemaSeeder {
        val seeder = DbSchemaSeeder(
            itemService = itemService,
            permissionService = permissionService
        )
        if (seedOnInit) {
            logger.info("DB schema seed flag enabled")
            seeder.seedDbSchema(readDbSchema())
        }

        return seeder
    }

    private fun readDbSchema() = DbSchemaReader().readDbSchema(dbSchemaPath)
}