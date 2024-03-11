package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.FindOneHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.service.ClassService
import ru.scisolutions.scicmscore.persistence.service.ItemService

@Service
class FindOneHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val aclItemRecDao: ACLItemRecDao,
    private val attributeValueHelper: AttributeValueHelper
) {
    fun findOne(itemName: String, id: String, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, FindOneHook::class.java)
        implInstance?.beforeFindOne(itemName, id)

        val itemRec =
            if (isOnlyId(attrNames))
                ItemRec().apply { this.id = id }
            else
                aclItemRecDao.findByIdForRead(item, id, attrNames)

        val response = Response(
            itemRec?.let { ItemRec(attributeValueHelper.prepareValuesToReturn(item, itemRec)) }
        )

        implInstance?.afterFindOne(itemName, response)

        return response
    }

    private fun isOnlyId(attrNames: Set<String>): Boolean = attrNames.size == 1 && ID_ATTR_NAME in attrNames

    fun findOneRelated(
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        selectAttrNames: Set<String>
    ): RelationResponse {
        val id = parentItemRec[parentAttrName] as String?
        if (id == null) {
            logger.trace("The attribute [$parentAttrName] is absent in the parent item, so it cannot be fetched")
            return RelationResponse()
        }

        val item = itemService.getByName(itemName)
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val itemRec =
            if (isOnlyId(attrNames))
                ItemRec().apply { this.id = id }
            else
                aclItemRecDao.findByIdForRead(item, id, attrNames)

        return RelationResponse(
            itemRec?.let { ItemRec(attributeValueHelper.prepareValuesToReturn(item, it)) }
        )
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private val logger = LoggerFactory.getLogger(FindOneHandler::class.java)
    }
}