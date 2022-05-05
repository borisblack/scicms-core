package ru.scisolutions.scicmscore.engine.schema.reader

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.streams.toList

@Service
class FileModelsReader(
    private val schemaProps: SchemaProps
) : ModelsReader {
    override fun read(): Collection<AbstractModel> {
        val schemaPath = schemaProps.path ?: throw IllegalStateException("Schema path is not set")
        logger.info("Reading the models path [{}]", schemaPath)
        val models = Files.walk(Paths.get(schemaPath))
            .filter(Files::isRegularFile)
            .map { readModel(it) }
            .toList()

        logger.info("Read {} models", models.size)

        return models
    }

    private fun readModel(path: Path): AbstractModel {
        logger.info("Reading the model file [{}]", path)
        return when (path.extension) {
            "yml", "yaml" -> yamlMapper.readValue(path.toFile(), AbstractModel::class.java)
            "json" -> jsonMapper.readValue(path.toFile(), AbstractModel::class.java)
            else -> throw IllegalArgumentException("Extension ${path.extension} is not supported")
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FileModelsReader::class.java)

        private val yamlMapper by lazy {
            ObjectMapper(YAMLFactory())
                .registerModule(kotlinModule())
        }

        private val jsonMapper by lazy {
            jacksonObjectMapper()
        }
    }
}