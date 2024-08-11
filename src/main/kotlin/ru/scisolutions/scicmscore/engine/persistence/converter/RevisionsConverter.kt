package ru.scisolutions.scicmscore.engine.persistence.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class RevisionsConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attribute: List<String>): String = attribute.joinToString(" ")

    override fun convertToEntityAttribute(dbData: String): List<String> = dbData.split(whitespaceRegex)

    companion object {
        private val whitespaceRegex = "\\s+".toRegex()
    }
}
