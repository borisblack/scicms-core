package ru.iss.dms.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.scisolutions.scicmscore.dbschema.model.Spec
import javax.persistence.AttributeConverter
import javax.persistence.Converter

private val objectMapper = jacksonObjectMapper()

@Converter
class SpecConverter : AttributeConverter<Spec, String> {
    override fun convertToDatabaseColumn(attribute: Spec): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): Spec = objectMapper.readValue(dbData, Spec::class.java)
}