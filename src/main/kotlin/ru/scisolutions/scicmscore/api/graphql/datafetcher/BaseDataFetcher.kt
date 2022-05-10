package ru.scisolutions.scicmscore.api.graphql.datafetcher

import graphql.schema.GraphQLList
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType
import java.util.regex.Pattern

open class BaseDataFetcher {
    protected val responseFieldTypePattern: Pattern = Pattern.compile("^(\\w+)Response$")

    protected fun extractCapitalizedItemNameFromFieldType(fieldType: String) = getFirstMatch(getFieldTypePattern(), fieldType)

    private fun getFirstMatch(pattern: Pattern, input: String): String {
        val matcher = pattern.matcher(input)
        return if (matcher.matches()) {
            matcher.group(1)
        } else {
            throw IllegalArgumentException("Input [$input] does not match pattern [$pattern]")
        }
    }

    protected open fun getFieldTypePattern(): Pattern = throw UnsupportedOperationException("The method must be overridden")

    protected fun parseFieldType(fieldType: GraphQLType): String =
        when (fieldType) {
            is GraphQLList -> parseFieldType(fieldType.wrappedType)
            is GraphQLNonNull -> parseFieldType(fieldType.wrappedType)
            else -> (fieldType as GraphQLNamedType).name
        }
}