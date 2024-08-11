package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.flaggedResponseFieldTypeRegex
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse
import ru.scisolutions.scicmscore.extension.lowerFirst

@Component
class UnlockDataFetcher(
    private val engine: Engine
) : DataFetcher<DataFetcherResult<FlaggedResponse>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<FlaggedResponse> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(flaggedResponseFieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val selectAttrNames = dfe.selectDataFields()
        val id = dfe.arguments[ID_ARG_NAME] as String? ?: throw IllegalArgumentException("ID argument is null.")
        val result = engine.unlock(itemName, id, selectAttrNames)

        return DataFetcherResult.newResult<FlaggedResponse>()
            .data(result)
            .build()
    }

    companion object {
        private const val ID_ARG_NAME = "id"
    }
}
