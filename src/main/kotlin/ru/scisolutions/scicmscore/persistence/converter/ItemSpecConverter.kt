package ru.scisolutions.scicmscore.persistence.converter

import ru.scisolutions.scicmscore.model.ItemSpec
import ru.scisolutions.scicmscore.util.Json
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class ItemSpecConverter : AttributeConverter<ItemSpec, String> {
    override fun convertToDatabaseColumn(attribute: ItemSpec): String = Json.objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): ItemSpec = Json.objectMapper.readValue(dbData, ItemSpec::class.java)
}