package ru.scisolutions.scicmscore.engine.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.db.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.db.query.FindAllQueryBuilder
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.FindAllHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.service.ClassService
import ru.scisolutions.scicmscore.persistence.service.ItemService

@Service
class FindAllHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val findAllQueryBuilder: FindAllQueryBuilder,
    private val itemRecDao: ItemRecDao
) {
    fun findAll(
        itemName: String,
        input: FindAllInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): ResponseCollection {
        val item = itemService.getByName(itemName)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val paramSource = AttributeSqlParameterSource()
        val findAllQuery = findAllQueryBuilder.buildFindAllQuery(
            item = item,
            input = input,
            selectAttrNames = attrNames,
            selectPaginationFields = selectPaginationFields,
            paramSource = paramSource
        )

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, FindAllHook::class.java)
        implInstance?.beforeFindAll(itemName, input)

        val itemRecList: List<ItemRec> = itemRecDao.findAll(item, findAllQuery.sql, paramSource)

        val response = ResponseCollection(
            data = itemRecList,
            meta = ResponseCollectionMeta(
                pagination = findAllQuery.pagination
            )
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
        selectPaginationFields: Set<String>
    ): RelationResponseCollection {
        val item = itemService.getByName(itemName)
        val parentItem = itemService.getByName(parentItemName)
        val parentId = parentItemRec.id ?: throw IllegalArgumentException("Parent ID not found")
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val paramSource = AttributeSqlParameterSource()
        val findAllQuery = findAllQueryBuilder.buildFindAllRelatedQuery(
            parentItem = parentItem,
            parentId = parentId,
            parentAttrName = parentAttrName,
            item = item,
            input = input,
            selectAttrNames = attrNames,
            selectPaginationFields = selectPaginationFields,
            paramSource = paramSource
        )
        val itemRecList: List<ItemRec> = itemRecDao.findAll(item, findAllQuery.sql, paramSource)

        return RelationResponseCollection(
            data = itemRecList,
            meta = ResponseCollectionMeta(
                pagination = findAllQuery.pagination
            )
        )
    }
}