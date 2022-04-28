package ru.scisolutions.scicmscore.api.graphql.datafetcher

import java.util.regex.Pattern

object DataFetcherUtil {
    fun parseItemName(fieldName: String, fieldType: String, fieldTypePattern: Pattern): String {
        val fieldTypeMatcher = fieldTypePattern.matcher(fieldType)
        return if (fieldTypeMatcher.matches()) {
            fieldTypeMatcher.group(1)
        } else {
            throw IllegalArgumentException("Field [$fieldName] has invalid type ($fieldType)")
        }
    }
}