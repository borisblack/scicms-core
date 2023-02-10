package ru.scisolutions.scicmscore.persistence.converter

import com.fasterxml.jackson.core.type.TypeReference
import ru.scisolutions.scicmscore.util.Json
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class LinkedHashSetConverter<T> : AttributeConverter<LinkedHashSet<T>, String> {
    override fun convertToDatabaseColumn(attribute: LinkedHashSet<T>): String = Json.objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): LinkedHashSet<T> = Json.objectMapper.readValue(dbData, object: TypeReference<LinkedHashSet<T>>() {})
}