package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.responseCollectionFieldTypeRegex
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.mapper.FindAllInputMapper
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection
import ru.scisolutions.scicmscore.util.lowerFirst

@Component
class FindAllDataFetcher(
    private val findAllInputMapper: FindAllInputMapper,
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<ResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<ResponseCollection> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(responseCollectionFieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val selectAttrNames = dfe.selectDataFields()
        val responseCollectionInput = findAllInputMapper.mapToResponseCollectionInput(itemName, dfe.arguments)
        val selectPaginationFields = dfe.selectionSet.getFields("meta/pagination/*").asSequence()
            .map { it.name }
            .toSet()

        val result = dataEngine.findAll(itemName, responseCollectionInput, selectAttrNames, selectPaginationFields)

        return DataFetcherResult.newResult<ResponseCollection>()
            .data(result)
            .build()
    }
}