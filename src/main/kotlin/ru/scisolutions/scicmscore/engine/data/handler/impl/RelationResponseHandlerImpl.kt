package ru.scisolutions.scicmscore.engine.data.handler.impl

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
    override fun getRelationResponse(sourceItemRec: ItemRec, itemName: String, fields: Set<String>): RelationResponse {
        val item = itemService.getItem(itemName)
        val itemRec = itemRecDao.findById(item, sourceItemRec[itemName] as String, fields)

        return RelationResponse(itemRec)
    }
}