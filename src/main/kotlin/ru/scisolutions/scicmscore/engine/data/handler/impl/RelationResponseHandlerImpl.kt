package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.db.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.RelationResponseHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.RelationResponse
import ru.scisolutions.scicmscore.service.ItemService

@Service
class RelationResponseHandlerImpl(
    private val itemService: ItemService,
    private val itemRecDao: ItemRecDao,
) : RelationResponseHandler {
    override fun getRelationResponse(itemName: String, fields: Set<String>, sourceItemRec: ItemRec, fieldName: String): RelationResponse {
        val item = itemService.getItem(itemName)
        val id = sourceItemRec[fieldName] as String?
        val itemRec = if (id == null) {
            logger.info("Field [$fieldName] is null, so it cannot be fetched")
            null
        } else
            itemRecDao.findById(item, id, fields)

        return RelationResponse(itemRec)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RelationResponseHandlerImpl::class.java)
    }
}