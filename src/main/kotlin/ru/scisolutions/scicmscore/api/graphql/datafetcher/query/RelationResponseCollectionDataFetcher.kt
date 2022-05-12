package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.BaseDataFetcher
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.mapper.FindAllInputMapper
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponseCollection
import java.util.regex.Pattern

@Component
class RelationResponseCollectionDataFetcher(
    private val findAllInputMapper: FindAllInputMapper,
    private val dataEngine: DataEngine
) : BaseDataFetcher(), DataFetcher<DataFetcherResult<RelationResponseCollection>> {
    override fun getFieldTypePattern(): Pattern = Pattern.compile("^(\\w+)RelationResponseCollection$")

    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponseCollection> {
        val capitalizedParentItemName = parseFieldType(dfe.parentType)
        val parentItemName = capitalizedParentItemName.decapitalize()
        val fieldType = parseFieldType(dfe.fieldType)
        val capitalizedItemName = extractCapitalizedItemNameFromFieldType(fieldType)
        val itemName = capitalizedItemName.decapitalize()
        val sourceItemRec: ItemRec = dfe.getSource()
        val attrName = dfe.field.name
        val responseCollectionInput = findAllInputMapper.mapToRelationResponseCollectionInput(itemName, dfe.arguments)
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