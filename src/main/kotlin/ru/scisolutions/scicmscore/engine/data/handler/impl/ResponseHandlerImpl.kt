package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.db.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.ResponseHandler
import ru.scisolutions.scicmscore.engine.data.model.Response
import ru.scisolutions.scicmscore.service.ItemService

@Service
class ResponseHandlerImpl(
    private val itemService: ItemService,
    private val itemRecDao: ItemRecDao,
) : ResponseHandler {
    override fun getResponse(itemName: String, fields: Set<String>, id: String): Response {
        val item = itemService.getItem(itemName)
        val itemRec = itemRecDao.findById(item, id, fields)

        return Response(itemRec)
    }
}