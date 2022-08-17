package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.response.Response

@Component
class FindOneDataFetcher(
    private val engine: Engine
) : DataFetcher<DataFetcherResult<Response>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        val selectAttrNames = dfe.selectDataFields()
        val id = dfe.arguments[ID_ARG_NAME] as String? ?: throw IllegalArgumentException("ID argument is null.")
        val itemName = dfe.field.name
        val result = engine.findOne(itemName, id, selectAttrNames)

        return DataFetcherResult.newResult<Response>()
            .data(result)
            .build()
    }

    companion object {
        private const val ID_ARG_NAME = "id"
    }
}