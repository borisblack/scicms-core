package ru.scisolutions.scicmscore.api.graphql.datafetcher

import graphql.schema.GraphQLList
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType
import java.util.regex.Pattern

object DataFetcherUtil {
    private val relationResponseFieldTypePattern = Pattern.compile("^(\\w+)RelationResponse$")
    private val responseCollectionFieldTypePattern = Pattern.compile("^(\\w+)ResponseCollection$")
    private val customMethodResponseFieldTypePattern = Pattern.compile("^(\\w+)CustomMethodResponse$")

    fun parseFieldType(fieldType: GraphQLType): String =
        when (fieldType) {
            is GraphQLList -> parseFieldType(fieldType.wrappedType)
            is GraphQLNonNull -> parseFieldType(fieldType.wrappedType)
            else -> (fieldType as GraphQLNamedType).name
        }

    fun extractCapitalizedItemNameFromRelationResponseFieldType(fieldType: String) = getFirstMatch(relationResponseFieldTypePattern, fieldType)

    fun extractCapitalizedItemNameFromResponseCollectionFieldType(fieldType: String) = getFirstMatch(responseCollectionFieldTypePattern, fieldType)

    fun extractCapitalizedItemNameFromCustomMethodResponseFieldType(fieldType: String) = getFirstMatch(customMethodResponseFieldTypePattern, fieldType)

    private fun getFirstMatch(pattern: Pattern, input: String): String {
        val matcher = pattern.matcher(input)
        return if (matcher.matches()) {
            matcher.group(1)
        } else {
            throw IllegalArgumentException("Input [$input] does not match pattern [$pattern]")
        }
    }
}