package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.BaseDataFetcher
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import java.util.regex.Pattern

@Component
class RelationResponseDataFetcher(
    private val dataEngine: DataEngine
) : BaseDataFetcher(), DataFetcher<DataFetcherResult<RelationResponse>> {
    override fun getFieldTypePattern(): Pattern = Pattern.compile("^(\\w+)RelationResponse$")

    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponse> {
        val capitalizedParentItemName = parseFieldType(dfe.parentType)
        val parentItemName = capitalizedParentItemName.decapitalize()
        val fieldType = parseFieldType(dfe.fieldType)
        val capitalizedItemName = extractCapitalizedItemNameFromFieldType(fieldType)
        val itemName = capitalizedItemName.decapitalize()
        val sourceItemRec: ItemRec = dfe.getSource()
        val attrName = dfe.field.name
        val selectAttrNames = dfe.selectionSet.getFields("data/*").asSequence() // root fields
            .map { it.name }
            .toSet()
            .ifEmpty { throw IllegalArgumentException("Selection set is empty") }

        val result = dataEngine.getRelationResponse(
            parentItemName,
            itemName,
            sourceItemRec,
            attrName,
            selectAttrNames
        )

        return DataFetcherResult.newResult<RelationResponse>()
            .data(result)
            .build()
    }
}