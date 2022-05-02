package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

class PurgeDataFetcher : DataFetcher<DataFetcherResult<ResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<ResponseCollection> {
        return DataFetcherResult.newResult<ResponseCollection>()
            .data(ResponseCollection(emptyList()))
            .build()
    }
}