package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.responseFieldTypeRegex
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.extension.lowerFirst

@Component
class CreateVersionDataFetcher(
    private val engine: Engine
) : DataFetcher<DataFetcherResult<Response>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(responseFieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val selectAttrNames = dfe.selectDataFields()
        val input =
            CreateVersionInput(
                id = dfe.arguments[ID_ARG_NAME] as String? ?: throw IllegalArgumentException("ID argument is null."),
                data = dfe.arguments[DATA_ARG_NAME] as Map<String, Any?>? ?: throw IllegalArgumentException("The [$DATA_ARG_NAME] argument is null."),
                majorRev = dfe.arguments[MAJOR_REV_ARG_NAME] as String?,
                locale = dfe.arguments[LOCALE_ARG_NAME] as String?,
                copyCollectionRelations = dfe.arguments[COPY_COLLECTION_RELATIONS_ARG_NAME] as Boolean?
            )

        val result = engine.createVersion(itemName, input, selectAttrNames)

        return DataFetcherResult.newResult<Response>()
            .data(result)
            .build()
    }

    companion object {
        private const val ID_ARG_NAME = "id"
        private const val DATA_ARG_NAME = "data"
        private const val MAJOR_REV_ARG_NAME = "majorRev"
        private const val LOCALE_ARG_NAME = "locale"
        private const val COPY_COLLECTION_RELATIONS_ARG_NAME = "copyCollectionRelations"
    }
}
