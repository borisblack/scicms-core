package ru.scisolutions.scicmscore.engine.persistence.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import ru.scisolutions.scicmscore.engine.model.DatasetSpec
import ru.scisolutions.scicmscore.util.Json

@Converter
class DatasetSpecConverter : AttributeConverter<DatasetSpec, String> {
    override fun convertToDatabaseColumn(attribute: DatasetSpec): String = Json.objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): DatasetSpec = Json.objectMapper.readValue(dbData, DatasetSpec::class.java)
}