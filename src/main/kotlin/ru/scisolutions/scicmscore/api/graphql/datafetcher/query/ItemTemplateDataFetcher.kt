package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import ru.scisolutions.scicmscore.engine.schema.model.DbSchema
import ru.scisolutions.scicmscore.engine.schema.model.ItemTemplate

@DgsComponent
class ItemTemplateDataFetcher(private val dbSchema: DbSchema) {
    @DgsQuery
    fun itemTemplates(): List<ItemTemplate> = dbSchema.listTemplates()
}