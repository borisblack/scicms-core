package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLObjectType
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.DataFetcherUtil
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.RelationResponse
import java.util.regex.Pattern

@Component
class RelationResponseDataFetcher(
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<RelationResponse>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<RelationResponse> {
        val fieldName = dfe.field.name
        val fieldType = (dfe.fieldType as GraphQLObjectType).name
        val capitalizedItemName = DataFetcherUtil.parseItemName(fieldName, fieldType, fieldTypePattern)
        val itemName = capitalizedItemName.decapitalize()
        val sourceItemRec: ItemRec = dfe.getSource()
        val dataField = dfe.selectionSet.fields[0]
        val fields = dataField.selectionSet.getFields("*").asSequence() // root fields
            .map { it.name }
            .toSet()

        val result = dataEngine.getRelationResponse(sourceItemRec, itemName, fields)

        return DataFetcherResult.newResult<RelationResponse>()
            .data(result)
            .build()
    }

    companion object {
        private val fieldTypePattern = Pattern.compile("(\\w+)RelationResponse")
    }
}