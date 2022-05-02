package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.response.Response

@Component
class ResponseDataFetcher(
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<Response>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        val itemName = dfe.field.name
        val dataField = dfe.selectionSet.fields[0]
        val fields = dataField.selectionSet.getFields("*").asSequence() // root fields
            .map { it.name }
            .toSet()

        val result = dataEngine.getResponse(itemName, fields, dfe.arguments[ID_ARG_NAME] as String)

        return DataFetcherResult.newResult<Response>()
            .data(result)
            .build()
    }

    companion object {
        private const val ID_ARG_NAME = "id"
    }
}