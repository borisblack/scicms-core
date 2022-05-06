package ru.scisolutions.scicmscore.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.engine.schema.reader.DbSchemaReader
import ru.scisolutions.scicmscore.engine.schema.seeder.SchemaSeeder

@Configuration
class SchemaConfig(
    schemaProps: SchemaProps,
    dbSchemaReader: DbSchemaReader,
    schemaSeeder: SchemaSeeder,
) {
    init {
        if (schemaProps.readOnInit) {
            logger.info("Schema read flag enabled. Trying to read")
            val schema = dbSchemaReader.read()

            if (schemaProps.seedOnInit) {
                logger.info("Schema seed flag enabled. Trying to seed")
                schemaSeeder.seed(schema)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchemaConfig::class.java)
    }
}