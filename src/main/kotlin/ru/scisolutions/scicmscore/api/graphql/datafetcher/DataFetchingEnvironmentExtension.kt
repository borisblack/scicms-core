package ru.scisolutions.scicmscore.api.graphql.datafetcher

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

val responseFieldTypeRegex = "^(\\w+)Response$".toRegex()
val flaggedResponseFieldTypeRegex = "^(\\w+)FlaggedResponse$".toRegex()
val responseCollectionFieldTypeRegex = "^(\\w+)ResponseCollection$".toRegex()

fun DataFetchingEnvironment.extractCapitalizedItemNameFromFieldType(fieldTypeRegex: Regex): String {
    val fieldType = this.unwrapFieldType()
    val matchResult = fieldTypeRegex.matchEntire(fieldType)
    return if (matchResult != null) {
        val (capitalizedItemName) = matchResult.destructured
        capitalizedItemName
    } else {
        throw IllegalArgumentException("Field [$fieldType] does not match pattern [$fieldTypeRegex]")
    }
}

fun DataFetchingEnvironment.unwrapParentType(): String = unwrapGraphQLType(this.parentType)

fun DataFetchingEnvironment.unwrapFieldType(): String = unwrapGraphQLType(this.fieldType)

private fun unwrapGraphQLType(graphQLType: GraphQLType): String =
    when (graphQLType) {
        is GraphQLList -> unwrapGraphQLType(graphQLType.wrappedType)
        is GraphQLNonNull -> unwrapGraphQLType(graphQLType.wrappedType)
        else -> (graphQLType as GraphQLNamedType).name
    }

fun DataFetchingEnvironment.selectDataFields() = this.selectionSet.getFields("data/*").asSequence()
    .map { it.name }
    .toSet()
    .ifEmpty { throw IllegalArgumentException("Selection set is empty") }