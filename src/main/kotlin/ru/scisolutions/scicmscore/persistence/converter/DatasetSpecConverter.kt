package ru.scisolutions.scicmscore.persistence.converter

import ru.scisolutions.scicmscore.model.DatasetSpec
import ru.scisolutions.scicmscore.util.Json
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class DatasetSpecConverter : AttributeConverter<DatasetSpec, String> {
    override fun convertToDatabaseColumn(attribute: DatasetSpec): String = Json.objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): DatasetSpec = Json.objectMapper.readValue(dbData, DatasetSpec::class.java)
}