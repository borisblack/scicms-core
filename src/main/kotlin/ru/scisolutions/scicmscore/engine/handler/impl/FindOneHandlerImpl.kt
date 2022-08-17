package ru.scisolutions.scicmscore.engine.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.handler.FindOneHandler
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.model.FindOneHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.persistence.service.ClassService
import ru.scisolutions.scicmscore.persistence.service.ItemService

@Service
class FindOneHandlerImpl(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val aclItemRecDao: ACLItemRecDao
) : FindOneHandler {
    override fun findOne(itemName: String, id: String, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, FindOneHook::class.java)
        implInstance?.beforeFindOne(itemName, id)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val itemRec =
            if (isOnlyId(attrNames))
                ItemRec().apply { this.id = id }
            else
                aclItemRecDao.findByIdForRead(item, id, attrNames)

        val response = Response(itemRec)

        implInstance?.afterFindOne(itemName,response)

        return response
    }

    private fun isOnlyId(attrNames: Set<String>): Boolean = attrNames.size == 1 && ID_ATTR_NAME in attrNames

    override fun findOneRelated(
        parentItemName: String,
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        selectAttrNames: Set<String>
    ): RelationResponse {
        val id = parentItemRec[parentAttrName] as String?
        if (id == null) {
            logger.debug("The attribute [$parentAttrName] is absent in the parent item, so it cannot be fetched")
            return RelationResponse()
        }

        val item = itemService.getByName(itemName)
        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val itemRec =
            if (isOnlyId(attrNames))
                ItemRec().apply { this.id = id }
            else
                aclItemRecDao.findByIdForRead(item, id, attrNames)

        return RelationResponse(itemRec)
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private val logger = LoggerFactory.getLogger(FindOneHandlerImpl::class.java)
    }
}