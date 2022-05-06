package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.DataFetcherUtil
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.mapper.ResponseCollectionInputMapper
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponseCollection

@Component
class RelationResponseCollectionDataFetcher(
    private val responseCollectionInputMapper: ResponseCollectionInputMapper,
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<RelationResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponseCollection> {
        val capitalizedParentItemName = DataFetcherUtil.parseFieldType(dfe.parentType)
        val parentItemName = capitalizedParentItemName.decapitalize()
        val fieldType = DataFetcherUtil.parseFieldType(dfe.fieldType)
        val capitalizedItemName = DataFetcherUtil.extractCapitalizedItemNameFromRelationResponseCollectionFieldType(fieldType)
        val itemName = capitalizedItemName.decapitalize()
        val sourceItemRec: ItemRec = dfe.getSource()
        val attrName = dfe.field.name
        val responseCollectionInput = responseCollectionInputMapper.mapToRelationResponseCollectionInput(itemName, dfe.arguments)
        val selectAttrNames = dfe.selectionSet.getFields("data/*").asSequence() // root fields
            .map { it.name }
            .toSet()
            .ifEmpty { throw IllegalArgumentException("Selection set is empty") }

        val selectPaginationFields = dfe.selectionSet.getFields("meta/pagination/*").asSequence()
            .map { it.name }
            .toSet()

        val result = dataEngine.getRelationResponseCollection(
            parentItemName = parentItemName,
            itemName = itemName,
            sourceItemRec = sourceItemRec,
            attrName = attrName,
            input = responseCollectionInput,
            selectAttrNames = selectAttrNames,
            selectPaginationFields = selectPaginationFields
        )

        return DataFetcherResult.newResult<RelationResponseCollection>()
            .data(result)
            .build()
    }
}