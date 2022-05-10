package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.BaseDataFetcher
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import java.util.regex.Pattern

@Component
class CreateDataFetcher(
    private val dataEngine: DataEngine
) : BaseDataFetcher(), DataFetcher<DataFetcherResult<Response>> {
    override fun getFieldTypePattern(): Pattern = responseFieldTypePattern

    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<Response> {
        val fieldType = parseFieldType(dfe.fieldType)
        val capitalizedItemName = extractCapitalizedItemNameFromFieldType(fieldType)
        val itemName = capitalizedItemName.decapitalize()
        val selectAttrNames = dfe.selectionSet.getFields("data/*").asSequence() // root fields
            .map { it.name }
            .toSet()
            .ifEmpty { throw IllegalArgumentException("Selection set is empty") }

        val data = dfe.arguments[DATA_ARG_NAME] as Map<String, Any?>? ?: throw IllegalArgumentException("Data argument is null")
        val result = dataEngine.create(itemName, data, selectAttrNames)

        return DataFetcherResult.newResult<Response>()
            .data(result)
            .build()
    }

    companion object {
        private const val DATA_ARG_NAME = "data"
    }
}