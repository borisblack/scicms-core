package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import ru.scisolutions.scicmscore.engine.data.model.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.data.model.ResponseCollection

class RelationResponseCollectionDataFetcher : DataFetcher<DataFetcherResult<RelationResponseCollection>> {
    override fun get(environment: DataFetchingEnvironment?): DataFetcherResult<RelationResponseCollection> {
        return DataFetcherResult.newResult<RelationResponseCollection>()
            .data(RelationResponseCollection(emptyList()))
            .build()
    }
}