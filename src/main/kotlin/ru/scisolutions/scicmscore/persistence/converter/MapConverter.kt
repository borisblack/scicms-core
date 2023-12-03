package ru.scisolutions.scicmscore.persistence.converter

import com.fasterxml.jackson.core.type.TypeReference
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import ru.scisolutions.scicmscore.util.Json

@Converter
class MapConverter : AttributeConverter<Map<String, Any?>?, String> {
    override fun convertToDatabaseColumn(attribute: Map<String, Any?>?): String? =
        if (attribute == null) null else Json.objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): Map<String, Any?>? =
        if (dbData == null) null else Json.objectMapper.readValue(dbData, object: TypeReference<Map<String, Any?>>() {})
}