package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.responseFieldTypeRegex
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.util.lowerFirst
import java.util.UUID

@Component
class UpdateDataFetcher(
    private val engine: Engine
) : DataFetcher<DataFetcherResult<Response>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(responseFieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val selectAttrNames = dfe.selectDataFields()
        val input = UpdateInput(
            id = dfe.arguments[ID_ARG_NAME] as UUID? ?: throw IllegalArgumentException("ID argument is null."),
            data = dfe.arguments[DATA_ARG_NAME] as Map<String, Any?>? ?: throw IllegalArgumentException("The [$DATA_ARG_NAME] argument is null."),
        )

        val result = engine.update(itemName, input, selectAttrNames)

        return DataFetcherResult.newResult<Response>()
            .data(result)
            .build()
    }

    companion object {
        private const val ID_ARG_NAME = "id"
        private const val DATA_ARG_NAME = "data"
    }
}