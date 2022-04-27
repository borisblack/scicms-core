package ru.scisolutions.scicmscore.persistence.converter

import org.slf4j.LoggerFactory
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class ItemImplementationConverter : AttributeConverter<Class<*>?, String?> {
    override fun convertToDatabaseColumn(attribute: Class<*>?): String? = attribute?.canonicalName

    override fun convertToEntityAttribute(dbData: String?): Class<*>? =
        if (dbData.isNullOrBlank()) {
            null
        } else {
            try {
                Class.forName(dbData)
            } catch (e: ClassNotFoundException) {
                logger.warn("Class [{}] not found", dbData)
                null
            }
        }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemImplementationConverter::class.java)
    }
}