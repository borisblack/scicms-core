package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.DataFetcherUtil
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.mapper.ResponseCollectionInputMapper
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollectionMeta

@Component
class ResponseCollectionDataFetcher(
    private val responseCollectionInputMapper: ResponseCollectionInputMapper,
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<ResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<ResponseCollection> {
        val fieldType = DataFetcherUtil.parseFieldType(dfe.fieldType)
        val capitalizedItemName = DataFetcherUtil.extractCapitalizedItemNameFromResponseCollectionFieldType(fieldType)
        val itemName = capitalizedItemName.decapitalize()
        val selectAttrNames = dfe.selectionSet.getFields("data/*").asSequence() // root fields
            .map { it.name }
            .toSet()
            .ifEmpty { throw IllegalArgumentException("Selection set is empty") }

        val responseCollectionInput = responseCollectionInputMapper.map(itemName, dfe.arguments)
        val selectPaginationFields = dfe.selectionSet.getFields("meta/pagination/*").asSequence()
            .map { it.name }
            .toSet()

        val result = dataEngine.getResponseCollection(itemName, responseCollectionInput, selectAttrNames, selectPaginationFields)

        return DataFetcherResult.newResult<ResponseCollection>()
            .data(result)
            .build()
    }
}