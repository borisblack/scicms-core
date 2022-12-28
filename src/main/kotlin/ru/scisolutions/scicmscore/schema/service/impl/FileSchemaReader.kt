package ru.scisolutions.scicmscore.schema.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.common.hash.Hashing
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.schema.model.AbstractModel
import ru.scisolutions.scicmscore.schema.model.DbSchema
import ru.scisolutions.scicmscore.schema.service.SchemaReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.streams.toList
import com.google.common.io.Files as GFiles

@Service
class FileSchemaReader(
    private val schemaProps: SchemaProps
) : SchemaReader {
    override fun read(): DbSchema {
        val schemaPath = schemaProps.path ?: throw IllegalStateException("Schema path is not set")
        logger.info("Reading the models path [{}]", schemaPath)
        val models = Files.walk(Paths.get(ClassLoader.getSystemResource(schemaPath).toURI()))
            .filter(Files::isRegularFile)
            .filter { !it.name.endsWith("schema.json") }
            .map { readModel(it.toFile()) }
            .toList()

        logger.info("Read {} models", models.size)

        val dbSchema = DbSchema()
        dbSchema.putModels(models)

        return dbSchema
    }

    private fun readModel(file: File): AbstractModel {
        logger.info("Reading the model file [{}]", file)
        return when (file.extension) {
            "yml", "yaml" -> {
                val model = yamlMapper.readValue(file, AbstractModel::class.java)
                val md5 = GFiles.asByteSource(file).hash(Hashing.md5())
                model.checksum = md5.toString()
                model
            }
            "json" -> {
                val model = jsonMapper.readValue(file, AbstractModel::class.java)
                val md5 = GFiles.asByteSource(file).hash(Hashing.md5())
                model.checksum = md5.toString()
                model
            }
            else -> throw IllegalArgumentException("Extension ${file.extension} is not supported")
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FileSchemaReader::class.java)

        private val yamlMapper by lazy {
            ObjectMapper(YAMLFactory())
                .registerModule(kotlinModule())
        }

        private val jsonMapper by lazy {
            jacksonObjectMapper()
        }
    }
}