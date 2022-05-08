package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.ResponseHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@Service
class ResponseHandlerImpl(
    private val itemService: ItemService,
    private val itemRecDao: ItemRecDao
) : ResponseHandler {
    override fun getResponse(itemName: String, id: String, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        val attrNames = adjustAttrNames(item, selectAttrNames)
        val itemRec = itemRecDao.findByIdForRead(item, id, attrNames)

        return Response(itemRec)
    }

    override fun getRelationResponse(
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
        val attrNames = adjustAttrNames(item, selectAttrNames)
        val itemRec = itemRecDao.findByIdForRead(item, id, attrNames)

        return RelationResponse(itemRec)
    }

    private fun adjustAttrNames(item: Item, selectAttrNames: Set<String>): Set<String> =
        selectAttrNames.asSequence()
            .filter {
                val attribute = item.spec.getAttributeOrThrow(it)
                !attribute.isCollection()
            }
            .plus(ID_ATTR_NAME)
            .toSet()

    companion object {
        private const val ID_ATTR_NAME = "id"

        private val logger = LoggerFactory.getLogger(ResponseHandlerImpl::class.java)
    }
}