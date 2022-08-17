package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.model.response.CustomMethodResponse
import ru.scisolutions.scicmscore.util.lowerFirst

@Component
class CustomMethodDataFetcher(
    private val engine: Engine
) : DataFetcher<DataFetcherResult<CustomMethodResponse>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<CustomMethodResponse> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(fieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val methodName = dfe.field.name.substringBefore(capitalizedItemName)
        val result = engine.callCustomMethod(itemName, methodName, CustomMethodInput(dfe.arguments[DATA_ARG_NAME]))

        return DataFetcherResult.newResult<CustomMethodResponse>()
            .data(result)
            .build()
    }

    companion object {
        private const val DATA_ARG_NAME = "data"

        private val fieldTypeRegex = "^(\\w+)CustomMethodResponse$".toRegex()
    }
}