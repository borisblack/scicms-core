package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import ru.scisolutions.scicmscore.engine.data.model.RelationResponse

class RelationResponseDataFetcher : DataFetcher<DataFetcherResult<RelationResponse>> {
    override fun get(environment: DataFetchingEnvironment?): DataFetcherResult<RelationResponse> {
        return DataFetcherResult.newResult<RelationResponse>()
            .data(RelationResponse())
            .build()
    }
}