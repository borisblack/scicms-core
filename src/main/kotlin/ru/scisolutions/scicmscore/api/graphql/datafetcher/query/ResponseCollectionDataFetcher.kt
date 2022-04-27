package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import ru.scisolutions.scicmscore.engine.data.model.ResponseCollection

class ResponseCollectionDataFetcher : DataFetcher<DataFetcherResult<ResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<ResponseCollection> {
        return DataFetcherResult.newResult<ResponseCollection>()
            .data(ResponseCollection(emptyList()))
            .build()
    }
}