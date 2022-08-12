package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.responseFieldTypeRegex
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.util.lowerFirst

@Component
class CreateDataFetcher(
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<Response>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(responseFieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val selectAttrNames = dfe.selectDataFields()
        val input = CreateInput(
            data = dfe.arguments[DATA_ARG_NAME] as Map<String, Any?>? ?: throw IllegalArgumentException("The [$DATA_ARG_NAME] argument is null."),
            majorRev = dfe.arguments[MAJOR_REV_ARG_NAME] as String?,
            locale = dfe.arguments[LOCALE_ARG_NAME] as String?
        )

        val result = dataEngine.create(itemName, input, selectAttrNames)

        return DataFetcherResult.newResult<Response>()
            .data(result)
            .build()
    }

    companion object {
        private const val DATA_ARG_NAME = "data"
        private const val MAJOR_REV_ARG_NAME = "majorRev"
        private const val LOCALE_ARG_NAME = "locale"
    }
}