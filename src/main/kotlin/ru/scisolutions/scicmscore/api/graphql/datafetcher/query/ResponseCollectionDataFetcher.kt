package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.DataFetcherUtil
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.mapper.ResponseCollectionInputMapper
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

@Component
class ResponseCollectionDataFetcher(
    private val responseCollectionInputMapper: ResponseCollectionInputMapper,
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<ResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<ResponseCollection> {
        val fieldType = DataFetcherUtil.parseFieldType(dfe.fieldType)
        val capitalizedItemName = DataFetcherUtil.extractCapitalizedItemNameFromResponseCollectionFieldType(fieldType)
        val itemName = capitalizedItemName.decapitalize()
        val dataField = dfe.selectionSet.fields[0]
        val selectAttrNames = dataField.selectionSet.getFields("*").asSequence() // root fields
            .map { it.name }
            .toSet()

        val responseCollectionInput = responseCollectionInputMapper.map(itemName, dfe.arguments)
        val result = dataEngine.getResponseCollection(itemName, responseCollectionInput, selectAttrNames)

        return DataFetcherResult.newResult<ResponseCollection>()
            .data(result)
            .build()
    }
}