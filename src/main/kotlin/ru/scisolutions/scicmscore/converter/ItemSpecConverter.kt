package ru.scisolutions.scicmscore.converter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.scisolutions.scicmscore.domain.model.ItemSpec
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class ItemSpecConverter : AttributeConverter<ItemSpec, String> {
    override fun convertToDatabaseColumn(attribute: ItemSpec): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): ItemSpec = objectMapper.readValue(dbData, ItemSpec::class.java)

    companion object {
        private val objectMapper = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}