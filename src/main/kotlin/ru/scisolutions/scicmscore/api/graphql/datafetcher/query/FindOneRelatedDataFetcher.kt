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
import ru.scisolutions.scicmscore.api.graphql.datafetcher.unwrapParentType
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.extension.lowerFirst
import java.util.concurrent.CompletableFuture

@Component
class FindOneRelatedDataFetcher(
    private val itemService: ItemService,
    private val dataLoaderBuilder: DataLoaderBuilder
) : DataFetcher<CompletableFuture<RelationResponse>> {
    override fun get(dfe: DataFetchingEnvironment): CompletableFuture<RelationResponse> {
        val capitalizedItemName = dfe.extractCapitalizedItemNameFromFieldType(fieldTypeRegex)
        val itemName = capitalizedItemName.lowerFirst()
        val parentItemName = dfe.unwrapParentType().lowerFirst()
        val parentItemRec: ItemRec = requireNotNull(dfe.getSource())
        val parentAttrName = dfe.field.name
        val selectAttrNames = dfe.selectDataFields()

        val key = parentItemRec[parentAttrName] as String?
        if (key == null) {
            logger.trace("The attribute [$parentAttrName] is absent in the parent item, so it cannot be fetched")
            return CompletableFuture.supplyAsync { RelationResponse() }
        }

        val parentItem = itemService.getByName(parentItemName)
        val item = itemService.getByName(itemName)
        val dataLoaderName = generateDataLoaderName(parentItem, parentAttrName, item)
        registerDataLoaderIfAbsent(dfe, dataLoaderName, parentItem, parentAttrName, item)
        val dataLoader: DataLoader<String, ItemRec> = requireNotNull(dfe.getDataLoader(dataLoaderName))

        val parentAttribute = parentItem.spec.getAttribute(parentAttrName)
        val keyAttrName = parentAttribute.referencedBy ?: item.idAttribute
        if (selectAttrNames.size == 1 && keyAttrName in selectAttrNames)
            return CompletableFuture.supplyAsync { RelationResponse(ItemRec().apply { this[keyAttrName] = key }) }

        val res = dataLoader.load(key)
        dataLoader.dispatch()

        return res.handle { itemRec, err ->
            if (err != null)
                throw err

            RelationResponse(itemRec)
        }
    }

    private fun generateDataLoaderName(parentItem: Item, parentAttrName: String, item: Item): String {
        val parentAttribute = parentItem.spec.getAttribute(parentAttrName)
        return if (parentAttribute.referencedBy == null) item.name else "${parentItem.name}#$parentAttrName"
    }

    private fun registerDataLoaderIfAbsent(
        dfe: DataFetchingEnvironment,
        dataLoaderName: String,
        parentItem: Item,
        parentAttrName: String,
        item: Item
    ) {
        if (dfe.getDataLoader<String, ItemRec>(dataLoaderName) == null)
            dfe.dataLoaderRegistry.register(
                dataLoaderName,
                DataLoaderFactory.newMappedDataLoader(dataLoaderBuilder.build(parentItem, parentAttrName, item))
            )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FindOneRelatedDataFetcher::class.java)
        private val fieldTypeRegex = "^(\\w+)RelationResponse$".toRegex()
    }
}