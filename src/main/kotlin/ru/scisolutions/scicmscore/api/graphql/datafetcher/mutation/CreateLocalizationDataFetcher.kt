package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.responseFieldTypeRegex
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

@Component
class CreateLocalizationDataFetcher(
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<Response>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(responseFieldTypeRegex)
        val itemName = capitalizedItemName.decapitalize()
        val selectAttrNames = dfe.selectDataFields()
        val input = CreateLocalizationInput(
            id = dfe.arguments[ID_ARG_NAME] as String? ?: throw IllegalArgumentException("ID argument is null"),
            data = dfe.arguments[DATA_ARG_NAME] as Map<String, Any?>? ?: throw IllegalArgumentException("Data argument is null"),
            locale = dfe.arguments[LOCALE_ARG_NAME] as String? ?: throw IllegalArgumentException("Locale argument is null")
        )

        val result = dataEngine.createLocalization(itemName, input, selectAttrNames)

        return DataFetcherResult.newResult<Response>()
            .data(result)
            .build()
    }

    companion object {
        private const val ID_ARG_NAME = "id"
        private const val DATA_ARG_NAME = "data"
        private const val LOCALE_ARG_NAME = "locale"
    }
}