package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.DataFetcherUtil
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse

@Component
class RelationResponseDataFetcher(
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<RelationResponse>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponse> {
        val capitalizedParentItemName = DataFetcherUtil.parseFieldType(dfe.parentType)
        val parentItemName = capitalizedParentItemName.decapitalize()

        val fieldType = DataFetcherUtil.parseFieldType(dfe.fieldType)
        val capitalizedItemName = DataFetcherUtil.extractCapitalizedItemNameFromRelationResponseFieldType(fieldType)
        val itemName = capitalizedItemName.decapitalize()

        val sourceItemRec: ItemRec = dfe.getSource()

        val attrName = dfe.field.name

        val dataField = dfe.selectionSet.fields[0]
        val selectedAttrNames = dataField.selectionSet.getFields("*").asSequence() // root fields
            .map { it.name }
            .toSet()

        val result = dataEngine.getRelationResponse(
            parentItemName,
            itemName,
            sourceItemRec,
            attrName,
            selectedAttrNames
        )

        return DataFetcherResult.newResult<RelationResponse>()
            .data(result)
            .build()
    }
}