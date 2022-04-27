package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import ru.scisolutions.scicmscore.engine.data.model.Response

class LockDataFetcher : DataFetcher<DataFetcherResult<Response>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        return DataFetcherResult.newResult<Response>()
            .data(Response())
            .build()
    }
}