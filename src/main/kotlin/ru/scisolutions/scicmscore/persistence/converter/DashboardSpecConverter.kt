package ru.scisolutions.scicmscore.persistence.converter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.scisolutions.scicmscore.model.DashboardSpec
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class DashboardSpecConverter : AttributeConverter<DashboardSpec, String> {
    override fun convertToDatabaseColumn(attribute: DashboardSpec): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): DashboardSpec = objectMapper.readValue(dbData, DashboardSpec::class.java)

    companion object {
        private val objectMapper = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}