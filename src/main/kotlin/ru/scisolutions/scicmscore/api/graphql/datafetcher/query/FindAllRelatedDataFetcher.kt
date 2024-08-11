package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.api.graphql.datafetcher.unwrapParentType
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.mapper.FindAllInputMapper
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.extension.lowerFirst

@Component
class FindAllRelatedDataFetcher(
    private val findAllInputMapper: FindAllInputMapper,
    private val engine: Engine,
) : DataFetcher<DataFetcherResult<RelationResponseCollection>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponseCollection> {
        val parentType = dfe.unwrapParentType()
        val parentItemName = parentType.lowerFirst()
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(fieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val parentItemRec: ItemRec = requireNotNull(dfe.getSource())
        val parentAttrName = dfe.field.name
        val responseCollectionInput = findAllInputMapper.mapToRelationResponseCollectionInput(itemName, dfe.arguments)
        val selectAttrNames = dfe.selectDataFields()
        val selectPaginationFields =
            dfe.selectionSet.getFields("meta/pagination/*").asSequence()
                .map { it.name }
                .toSet()

        val result =
            engine.findAllRelated(
                parentItemName = parentItemName,
                parentItemRec = parentItemRec,
                parentAttrName = parentAttrName,
                itemName = itemName,
                input = responseCollectionInput,
                selectAttrNames = selectAttrNames,
                selectPaginationFields = selectPaginationFields,
            )

        return DataFetcherResult.newResult<RelationResponseCollection>()
            .data(result)
            .build()
    }

    companion object {
        private val fieldTypeRegex = "^(\\w+)RelationResponseCollection$".toRegex()
    }
}
