package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.responseCollectionFieldTypeRegex
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

@Component
class PurgeDataFetcher(private val dataEngine: DataEngine) : DataFetcher<DataFetcherResult<ResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<ResponseCollection> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(responseCollectionFieldTypeRegex)
        val itemName = capitalizedItemName.decapitalize()
        val selectAttrNames = dfe.selectDataFields()
        val deletingStrategy = dfe.arguments[DELETING_STRATEGY_ARG_NAME] as String?
            ?: throw IllegalArgumentException("The [$DELETING_STRATEGY_ARG_NAME] argument is null.")

        val input = DeleteInput(
            id = dfe.arguments[ID_ARG_NAME] as String? ?: throw IllegalArgumentException("ID argument is null."),
            deletingStrategy = DeleteInput.DeletingStrategy.valueOf(deletingStrategy),
        )

        val result = dataEngine.purge(itemName, input, selectAttrNames)

        return DataFetcherResult.newResult<ResponseCollection>()
            .data(result)
            .build()
    }

    companion object {
        private const val ID_ARG_NAME = "id"
        private const val DELETING_STRATEGY_ARG_NAME = "deletingStrategy"
    }
}