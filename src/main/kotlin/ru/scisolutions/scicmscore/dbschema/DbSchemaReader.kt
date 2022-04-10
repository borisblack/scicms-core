package ru.scisolutions.scicmscore.dbschema

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.scisolutions.scicmscore.api.model.AbstractModel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.streams.toList

class DbSchemaReader {
    private val logger: Logger = LoggerFactory.getLogger(DbSchemaReader::class.java)

    private val yamlMapper by lazy {
        ObjectMapper(YAMLFactory())
            .registerModule(kotlinModule())
    }

    private val jsonMapper by lazy {
        jacksonObjectMapper()
    }

    fun readDbSchema(dbSchemaPath: String): DbSchema {
        logger.info("Reading DB schema path {}", dbSchemaPath)
        val dbSchema = DbSchema()
        val models = Files.walk(Paths.get(dbSchemaPath))
            .filter(Files::isRegularFile)
            .map { readItemModel(it) }
            .toList()

        dbSchema.addAll(models)

        return dbSchema
    }

    private fun readItemModel(path: Path): AbstractModel {
        logger.info("Trying to read the model path {}", path)
        return when (path.extension) {
            "yml", "yaml" -> yamlMapper.readValue(path.toFile(), AbstractModel::class.java)
            "json" -> jsonMapper.readValue(path.toFile(), AbstractModel::class.java)
            else -> throw IllegalArgumentException("Extension ${path.extension} is not supported")
        }
    }
}