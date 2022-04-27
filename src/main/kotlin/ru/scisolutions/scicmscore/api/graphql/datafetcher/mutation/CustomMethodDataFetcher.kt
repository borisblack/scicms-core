package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLObjectType
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodResponse
import java.util.regex.Pattern

@Component
class CustomMethodDataFetcher(
    private val dataEngine: DataEngine
) : DataFetcher<DataFetcherResult<CustomMethodResponse>> {
    override fun get(dfe: DataFetchingEnvironment): DataFetcherResult<CustomMethodResponse> {
        val inputData = dfe.arguments["data"]
        if (inputData !is Map<*, *>?)
            throw IllegalArgumentException("Input data must be of the Map type")

        val fieldName = dfe.field.name
        val fieldType = (dfe.fieldType as GraphQLObjectType).name
        val fieldTypeMatcher = fieldTypePattern.matcher(fieldType)
        val capitalizedItemName: String
        val itemName: String
        if (fieldTypeMatcher.matches()) {
            capitalizedItemName = fieldTypeMatcher.group(1)
            itemName = capitalizedItemName.decapitalize()
        } else {
            throw IllegalArgumentException("Field [$fieldName] has invalid type ($fieldType)")
        }

        val methodName = fieldName.substringBefore(capitalizedItemName)
        val result = dataEngine.callCustomMethod(
            itemName,
            methodName,
            CustomMethodInput(
                data = inputData as Map<String, Any?>?
            )
        )

        return DataFetcherResult.newResult<CustomMethodResponse>()
            .data(result)
            .build()
    }

    companion object {
        private val fieldTypePattern = Pattern.compile("(\\w+)CustomMethodResponse")
    }
}