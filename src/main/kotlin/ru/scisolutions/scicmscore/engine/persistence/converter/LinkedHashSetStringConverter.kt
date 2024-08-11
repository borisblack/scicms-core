package ru.scisolutions.scicmscore.engine.persistence.converter

import com.fasterxml.jackson.core.type.TypeReference
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import ru.scisolutions.scicmscore.util.Json

@Converter
class LinkedHashSetStringConverter : AttributeConverter<LinkedHashSet<String>, String> {
    override fun convertToDatabaseColumn(attribute: LinkedHashSet<String>): String = Json.objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): LinkedHashSet<String> = Json.objectMapper.readValue(
        dbData,
        object : TypeReference<LinkedHashSet<String>>() {},
    )
}
