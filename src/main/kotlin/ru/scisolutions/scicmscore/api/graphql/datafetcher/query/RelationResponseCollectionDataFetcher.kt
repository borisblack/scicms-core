package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponseCollection

class RelationResponseCollectionDataFetcher : DataFetcher<DataFetcherResult<RelationResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponseCollection> {
        return DataFetcherResult.newResult<RelationResponseCollection>()
            .data(RelationResponseCollection(emptyList()))
            .build()
    }
}