package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLObjectType
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.DataFetcherUtil
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodResponse
import java.util.regex.Pattern

@Component
class CustomMethodDataFetcher(
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<CustomMethodResponse>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<CustomMethodResponse> {
        val fieldName = dfe.field.name
        val fieldType = (dfe.fieldType as GraphQLObjectType).name
        val capitalizedItemName = DataFetcherUtil.parseItemName(fieldName, fieldType, fieldTypePattern)
        val itemName = capitalizedItemName.decapitalize()
        val methodName = fieldName.substringBefore(capitalizedItemName)
        val result = dataEngine.callCustomMethod(itemName, methodName, CustomMethodInput(dfe.arguments[DATA_ARG_NAME]))

        return DataFetcherResult.newResult<CustomMethodResponse>()
            .data(result)
            .build()
    }

    companion object {
        private const val DATA_ARG_NAME = "data"

        private val fieldTypePattern = Pattern.compile("(\\w+)CustomMethodResponse")
    }
}