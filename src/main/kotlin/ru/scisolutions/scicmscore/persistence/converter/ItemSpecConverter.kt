package ru.scisolutions.scicmscore.persistence.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import ru.scisolutions.scicmscore.model.ItemSpec
import ru.scisolutions.scicmscore.util.Json

@Converter
class ItemSpecConverter : AttributeConverter<ItemSpec, String> {
    override fun convertToDatabaseColumn(attribute: ItemSpec): String = Json.objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): ItemSpec = Json.objectMapper.readValue(dbData, ItemSpec::class.java)
}