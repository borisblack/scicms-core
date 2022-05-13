package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.api.graphql.datafetcher.unwrapParentType
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse

@Component
class RelationResponseDataFetcher(
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<RelationResponse>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponse> {
        val capitalizedParentItemName = dfe.unwrapParentType()
        val parentItemName = capitalizedParentItemName.decapitalize()
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(fieldTypeRegex)
        val itemName = capitalizedItemName.decapitalize()
        val sourceItemRec: ItemRec = dfe.getSource()
        val attrName = dfe.field.name
        val selectAttrNames = dfe.selectDataFields()
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

    companion object {
        private val fieldTypeRegex = "^(\\w+)RelationResponse$".toRegex()
    }
}