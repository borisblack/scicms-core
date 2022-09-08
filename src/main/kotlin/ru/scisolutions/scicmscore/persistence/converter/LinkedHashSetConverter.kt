package ru.scisolutions.scicmscore.persistence.converter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class LinkedHashSetConverter<T> : AttributeConverter<LinkedHashSet<T>, String> {
    override fun convertToDatabaseColumn(attribute: LinkedHashSet<T>): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): LinkedHashSet<T> = objectMapper.readValue(dbData, object: TypeReference<LinkedHashSet<T>>() {})

    companion object {
        private val objectMapper = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}