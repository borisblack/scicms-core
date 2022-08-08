package ru.scisolutions.scicmscore.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.engine.schema.model.DbSchema
import ru.scisolutions.scicmscore.engine.schema.reader.SchemaReader

@Configuration
class SchemaConfig(
    private val schemaProps: SchemaProps,
    private val schemaReader: SchemaReader
) {
    @Bean
    fun dbSchema() =
        if (schemaProps.readOnInit) {
            logger.info("Schema read flag enabled. Trying to read")
            schemaReader.read()
        } else {
            DbSchema()
        }

    companion object {
        private val logger = LoggerFactory.getLogger(SchemaConfig::class.java)
    }
}