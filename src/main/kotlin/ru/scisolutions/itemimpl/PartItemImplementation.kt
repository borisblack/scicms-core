package ru.scisolutions.itemimpl

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetchingEnvironment

class PartItemImplementation {
    fun send(dfe: DataFetchingEnvironment): DataFetcherResult<*> {
        return DataFetcherResult.newResult<Map<String, String>>()
            .data(mapOf("message" to "Success"))
            .build()
    }
}