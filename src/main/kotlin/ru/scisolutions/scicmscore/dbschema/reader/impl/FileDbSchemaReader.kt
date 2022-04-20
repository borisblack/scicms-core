package ru.scisolutions.scicmscore.dbschema.reader.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.AbstractModel
import ru.scisolutions.scicmscore.dbschema.DbSchema
import ru.scisolutions.scicmscore.dbschema.reader.DbSchemaReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.streams.toList

@Service
class FileDbSchemaReader(
    @Value("\${scicms-core.db-schema.path}")
    private val dbSchemaPath: String
) : DbSchemaReader {
    private val logger: Logger = LoggerFactory.getLogger(FileDbSchemaReader::class.java)

    private val yamlMapper by lazy {
        ObjectMapper(YAMLFactory())
            .registerModule(kotlinModule())
    }

    private val jsonMapper by lazy {
        jacksonObjectMapper()
    }

    override fun read(): DbSchema {
        logger.info("Reading the DB schema path [{}]", dbSchemaPath)
        val dbSchema = DbSchema()
        val models = Files.walk(Paths.get(dbSchemaPath))
            .filter(Files::isRegularFile)
            .map { readItemModel(it) }
            .toList()

        dbSchema.addAll(models)

        return dbSchema
    }

    private fun readItemModel(path: Path): AbstractModel {
        logger.info("Reading the model path [{}]", path)
        return when (path.extension) {
            "yml", "yaml" -> yamlMapper.readValue(path.toFile(), AbstractModel::class.java)
            "json" -> jsonMapper.readValue(path.toFile(), AbstractModel::class.java)
            else -> throw IllegalArgumentException("Extension ${path.extension} is not supported")
        }
    }
}