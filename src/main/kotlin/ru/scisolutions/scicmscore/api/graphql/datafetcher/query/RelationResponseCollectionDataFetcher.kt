package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.api.graphql.datafetcher.unwrapParentType
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.mapper.FindAllInputMapper
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponseCollection

@Component
class RelationResponseCollectionDataFetcher(
    private val findAllInputMapper: FindAllInputMapper,
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<RelationResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponseCollection> {
        val capitalizedParentItemName = dfe.unwrapParentType()
        val parentItemName = capitalizedParentItemName.decapitalize()
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(fieldTypeRegex)
        val itemName = capitalizedItemName.decapitalize()
        val sourceItemRec: ItemRec = dfe.getSource()
        val attrName = dfe.field.name
        val responseCollectionInput = findAllInputMapper.mapToRelationResponseCollectionInput(itemName, dfe.arguments)
        val selectAttrNames = dfe.selectDataFields()
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

    companion object {
        private val fieldTypeRegex = "^(\\w+)RelationResponseCollection$".toRegex()
    }
}