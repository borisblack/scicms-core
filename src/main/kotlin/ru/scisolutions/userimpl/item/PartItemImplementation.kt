package ru.scisolutions.userimpl.item

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetchingEnvironment

class PartItemImplementation {
    fun send(dfe: DataFetchingEnvironment): DataFetcherResult<*> {
        return DataFetcherResult.newResult<Map<String, String>>()
            .data(mapOf("message" to "Success"))
            .build()
    }
}