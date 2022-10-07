package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.response.Response

@Component
class FindOneDataFetcher(private val engine: Engine) : DataFetcher<Response> {
    override fun get(dfe: DataFetchingEnvironment): Response {
        val selectAttrNames = dfe.selectDataFields()
        val id = dfe.arguments[ID_ARG_NAME] as String? ?: throw IllegalArgumentException("ID argument is null.")
        val itemName = dfe.field.name

        return engine.findOne(itemName, id, selectAttrNames)
    }

    companion object {
        private const val ID_ARG_NAME = "id"
    }
}