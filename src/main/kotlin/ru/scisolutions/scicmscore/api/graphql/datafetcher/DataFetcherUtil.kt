package ru.scisolutions.scicmscore.api.graphql.datafetcher

import graphql.schema.GraphQLList
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType
import java.util.regex.Pattern

object DataFetcherUtil {
    fun parseFieldType(fieldType: GraphQLType): String =
        when (fieldType) {
            is GraphQLList -> parseFieldType(fieldType.wrappedType)
            is GraphQLNonNull -> parseFieldType(fieldType.wrappedType)
            else -> (fieldType as GraphQLNamedType).name
        }

    fun parseItemName(fieldName: String, fieldType: String, fieldTypePattern: Pattern): String {
        val fieldTypeMatcher = fieldTypePattern.matcher(fieldType)
        return if (fieldTypeMatcher.matches()) {
            fieldTypeMatcher.group(1)
        } else {
            throw IllegalArgumentException("Field [$fieldName] has invalid type ($fieldType)")
        }
    }
}