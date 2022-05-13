package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.mapper.FindAllInputMapper
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

@Component
class ResponseCollectionDataFetcher(
    private val findAllInputMapper: FindAllInputMapper,
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<ResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<ResponseCollection> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(fieldTypeRegex)
        val itemName = capitalizedItemName.decapitalize()
        val selectAttrNames = dfe.selectDataFields()
        val responseCollectionInput = findAllInputMapper.mapToResponseCollectionInput(itemName, dfe.arguments)
        val selectPaginationFields = dfe.selectionSet.getFields("meta/pagination/*").asSequence()
            .map { it.name }
            .toSet()

        val result = dataEngine.getResponseCollection(itemName, responseCollectionInput, selectAttrNames, selectPaginationFields)

        return DataFetcherResult.newResult<ResponseCollection>()
            .data(result)
            .build()
    }

    companion object {
        private val fieldTypeRegex = "^(\\w+)ResponseCollection$".toRegex()
    }
}