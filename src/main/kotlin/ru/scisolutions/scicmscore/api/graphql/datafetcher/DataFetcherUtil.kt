package ru.scisolutions.scicmscore.api.graphql.datafetcher

import graphql.schema.GraphQLList
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType
import java.util.regex.Pattern

object DataFetcherUtil {
    private val RELATION_RESPONSE_FIELD_TYPE_PATTERN = Pattern.compile("(\\w+)RelationResponse")
    private val CUSTOM_METHOD_RESPONSE_FIELD_TYPE_PATTERN = Pattern.compile("(\\w+)CustomMethodResponse")

    fun parseFieldType(fieldType: GraphQLType): String =
        when (fieldType) {
            is GraphQLList -> parseFieldType(fieldType.wrappedType)
            is GraphQLNonNull -> parseFieldType(fieldType.wrappedType)
            else -> (fieldType as GraphQLNamedType).name
        }

    fun extractItemNameFromRelationResponseFieldType(fieldType: String) = getFirstMatch(RELATION_RESPONSE_FIELD_TYPE_PATTERN, fieldType)

    fun extractItemNameFromCustomMethodResponseFieldType(fieldType: String) = getFirstMatch(CUSTOM_METHOD_RESPONSE_FIELD_TYPE_PATTERN, fieldType)

    private fun getFirstMatch(pattern: Pattern, input: String): String {
        val matcher = pattern.matcher(input)
        return if (matcher.matches()) {
            matcher.group(1)
        } else {
            throw IllegalArgumentException("Input [$input] does not match pattern [$pattern]")
        }
    }
}