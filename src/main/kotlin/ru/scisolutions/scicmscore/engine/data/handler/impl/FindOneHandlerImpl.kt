package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.FindOneHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.service.ItemService

@Service
class FindOneHandlerImpl(
    private val itemService: ItemService,
    private val itemRecDao: ItemRecDao
) : FindOneHandler {
    override fun findOne(itemName: String, id: String, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val itemRec =
            if (isOnlyId(attrNames))
                ItemRec().apply { this.id = id }
            else itemRecDao.findByIdForRead(item, id, attrNames)

        return Response(itemRec)
    }

    private fun isOnlyId(attrNames: Set<String>): Boolean = attrNames.size == 1 && ID_ATTR_NAME in attrNames

    override fun findOneRelated(
        parentItemName: String,
        itemName: String,
        sourceItemRec: ItemRec,
        attrName: String,
        selectAttrNames: Set<String>
    ): RelationResponse {
        val id = sourceItemRec[attrName] as String?
        if (id == null) {
            logger.debug("The attribute [$attrName] is absent in the source item, so it cannot be fetched")
            return RelationResponse()
        }

        val item = itemService.getByName(itemName)
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val itemRec =
            if (isOnlyId(attrNames))
                ItemRec().apply { this.id = id }
            else itemRecDao.findByIdForRead(item, id, attrNames)

        return RelationResponse(itemRec)
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private val logger = LoggerFactory.getLogger(FindOneHandlerImpl::class.java)
    }
}