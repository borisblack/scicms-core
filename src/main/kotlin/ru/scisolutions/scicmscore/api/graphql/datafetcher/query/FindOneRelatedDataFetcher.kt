package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.DataLoaderBuilder
import ru.scisolutions.scicmscore.api.graphql.datafetcher.extractCapitalizedItemNameFromFieldType
import ru.scisolutions.scicmscore.api.graphql.datafetcher.selectDataFields
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.util.lowerFirst
import java.util.concurrent.CompletableFuture

@Component
class FindOneRelatedDataFetcher(private val dataLoaderBuilder: DataLoaderBuilder) : DataFetcher<CompletableFuture<RelationResponse>> {
    override fun get(dfe: DataFetchingEnvironment): CompletableFuture<RelationResponse> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(fieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val parentItemRec: ItemRec = dfe.getSource()
        val parentAttrName = dfe.field.name
        val selectAttrNames = dfe.selectDataFields()

        val id = parentItemRec[parentAttrName] as String?
        if (id == null) {
            logger.trace("The attribute [$parentAttrName] is absent in the parent item, so it cannot be fetched")
            return CompletableFuture.supplyAsync { RelationResponse() }
        }

        registerDataLoaderIfAbsent(dfe, itemName)
        val dataLoader: DataLoader<String, ItemRec> = dfe.getDataLoader(itemName)

        if (selectAttrNames.size == 1 && ItemRec.ID_ATTR_NAME in selectAttrNames)
            return CompletableFuture.supplyAsync { RelationResponse(ItemRec().apply { this.id = id }) }

        val res = dataLoader.load(id)
        dataLoader.dispatch()

        return res.handle { itemRec, err ->
            if (err != null)
                throw err

            RelationResponse(itemRec)
        }
    }

    private fun registerDataLoaderIfAbsent(dfe: DataFetchingEnvironment, itemName: String) {
        if (dfe.getDataLoader<String, ItemRec>(itemName) == null)
            dfe.dataLoaderRegistry.register(
                itemName,
                DataLoaderFactory.newMappedDataLoader(dataLoaderBuilder.build(itemName))
            )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FindOneRelatedDataFetcher::class.java)
        private val fieldTypeRegex = "^(\\w+)RelationResponse$".toRegex()
    }
}