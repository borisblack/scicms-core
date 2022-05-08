package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.CreateHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import java.util.UUID

@Service
class CreateHandlerImpl(
    private val itemService: ItemService,
    private val itemRecDao: ItemRecDao
) : CreateHandler {
    override fun create(itemName: String, data: Map<String, Any?>, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (itemService.findByNameForCreate(item.name) == null)
            throw AccessDeniedException("Operation is prohibited")

        // TODO: Adjust data

        val newItemRec = ItemRec(data.toMutableMap()).apply {
            id = UUID.randomUUID().toString()
            configId = id
        }

        return Response(newItemRec)
    }

    private fun adjustData(item: Item, data: Map<String, Any?>): Map<String, Any?> =
        data.asSequence()
            .filter { (attrName, _) ->
                val attribute = item.spec.getAttributeOrThrow(attrName)
                !attribute.isCollection()
            }
            .map { (attrName, value) -> attrName to adjustAttribute(item, attrName, value) }
            .toMap()

    private fun <T> adjustAttribute(item: Item, attrName: String, value: T?): T? {
        TODO()
    }

    companion object {
        private const val ID_ATTR_NAME = "id"

        private val logger = LoggerFactory.getLogger(CreateHandlerImpl::class.java)
    }
}