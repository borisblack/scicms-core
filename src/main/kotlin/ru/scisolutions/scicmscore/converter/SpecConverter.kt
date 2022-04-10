package ru.scisolutions.scicmscore.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.scisolutions.scicmscore.api.model.Spec
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class SpecConverter : AttributeConverter<Spec, String> {
    override fun convertToDatabaseColumn(attribute: Spec): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): Spec = objectMapper.readValue(dbData, Spec::class.java)

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }
}