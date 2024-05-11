package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.responseFieldTypeRegex
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.extension.lowerFirst

@Component
class DeleteDataFetcher(private val engine: Engine) : DataFetcher<DataFetcherResult<Response>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(responseFieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val selectAttrNames = dfe.selectDataFields()
        val deletingStrategy = dfe.arguments[DELETING_STRATEGY_ARG_NAME] as String?
            ?: throw IllegalArgumentException("The [$DELETING_STRATEGY_ARG_NAME] argument is null.")

        val input = DeleteInput(
            id = dfe.arguments[ID_ARG_NAME] as String? ?: throw IllegalArgumentException("ID argument is null."),
            deletingStrategy = DeleteInput.DeletingStrategy.valueOf(deletingStrategy),
        )

        val result = engine.delete(itemName, input, selectAttrNames)

        return DataFetcherResult.newResult<Response>()
            .data(result)
            .build()
    }

    companion object {
        private const val ID_ARG_NAME = "id"
        private const val DELETING_STRATEGY_ARG_NAME = "deletingStrategy"
    }
}