package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.FindOneHook
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.service.ClassService

@Service
class FindOneHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val aclItemRecDao: ACLItemRecDao,
    private val attributeValueHelper: AttributeValueHelper,
) {
    fun findOne(itemName: String, id: String, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, FindOneHook::class.java)
        implInstance?.beforeFindOne(itemName, id)

        val isOnlyId = attrNames.size == 1 && item.idAttribute in attrNames
        val itemRec =
            if (isOnlyId) {
                ItemRec().apply { this[item.idAttribute] = id }
            } else {
                aclItemRecDao.findByIdForRead(item, id, attrNames)
            }

        val response =
            Response(
                itemRec?.let { ItemRec(attributeValueHelper.prepareValuesToReturn(item, itemRec)) },
            )

        implInstance?.afterFindOne(itemName, response)

        return response
    }

    fun findOneRelated(parentItemName: String, parentItemRec: ItemRec, parentAttrName: String, itemName: String, selectAttrNames: Set<String>): RelationResponse {
        val key = parentItemRec[parentAttrName] as String?
        if (key == null) {
            logger.trace("The attribute [$parentAttrName] is absent in the parent item, so it cannot be fetched")
            return RelationResponse()
        }

        val parentItem = itemService.getByName(parentItemName)
        val item = itemService.getByName(itemName)
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val parentAttribute = parentItem.spec.getAttribute(parentAttrName)
        val keyAttrName = parentAttribute.referencedBy ?: item.idAttribute
        val isOnlyKey = attrNames.size == 1 && keyAttrName in attrNames
        val itemRec =
            if (isOnlyKey) {
                ItemRec().apply { this[keyAttrName] = key }
            } else {
                aclItemRecDao.findByKeyForRead(item, keyAttrName, key, attrNames)
            }

        return RelationResponse(
            itemRec?.let { ItemRec(attributeValueHelper.prepareValuesToReturn(item, it)) },
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FindOneHandler::class.java)
    }
}
