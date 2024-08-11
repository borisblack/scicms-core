package ru.scisolutions.scicmscore.engine.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.FindAllHook
import ru.scisolutions.scicmscore.engine.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.persistence.query.FindAllQueryBuilder
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.service.ClassService

@Service
class FindAllHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val findAllQueryBuilder: FindAllQueryBuilder,
    private val itemRecDao: ItemRecDao,
    private val attributeValueHelper: AttributeValueHelper,
) {
    fun findAll(itemName: String, input: FindAllInput, selectAttrNames: Set<String>, selectPaginationFields: Set<String>): ResponseCollection {
        val item = itemService.getByName(itemName)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val paramSource = AttributeSqlParameterSource()
        val findAllQuery =
            findAllQueryBuilder.buildFindAllQuery(
                item = item,
                input = input,
                selectAttrNames = attrNames,
                selectPaginationFields = selectPaginationFields,
                paramSource = paramSource,
            )

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, FindAllHook::class.java)
        implInstance?.beforeFindAll(itemName, input)

        val itemRecList: List<ItemRec> =
            itemRecDao.findAll(item, findAllQuery.sql, paramSource)
                .map { ItemRec(attributeValueHelper.prepareValuesToReturn(item, it)) }

        val response =
            ResponseCollection(
                data = itemRecList,
                meta =
                ResponseCollectionMeta(
                    pagination = findAllQuery.pagination,
                ),
            )

        implInstance?.afterFindAll(itemName, response)

        return response
    }

    fun findAllRelated(
        parentItemName: String,
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        input: FindAllRelationInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>,
    ): RelationResponseCollection {
        val item = itemService.getByName(itemName)
        val parentItem = itemService.getByName(parentItemName)
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val paramSource = AttributeSqlParameterSource()
        val findAllQuery =
            findAllQueryBuilder.buildFindAllRelatedQuery(
                parentItem = parentItem,
                parentItemRec = parentItemRec,
                parentAttrName = parentAttrName,
                item = item,
                input = input,
                selectAttrNames = attrNames,
                selectPaginationFields = selectPaginationFields,
                paramSource = paramSource,
            )
        val itemRecList: List<ItemRec> =
            itemRecDao.findAll(item, findAllQuery.sql, paramSource)
                .map { ItemRec(attributeValueHelper.prepareValuesToReturn(item, it)) }

        return RelationResponseCollection(
            data = itemRecList,
            meta =
            ResponseCollectionMeta(
                pagination = findAllQuery.pagination,
            ),
        )
    }
}
