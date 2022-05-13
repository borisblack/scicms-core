package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.UpdateHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.handler.util.RelationHelper
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.data.service.PermissionManager
import ru.scisolutions.scicmscore.service.ItemService

@Service
class UpdateHandlerImpl(
    private val itemService: ItemService,
    private val attributeValueHelper: AttributeValueHelper,
    private val permissionManager: PermissionManager,
    private val auditManager: AuditManager,
    private val relationHelper: RelationHelper,
    private val itemRecDao: ItemRecDao,
) : UpdateHandler {
    override fun update(itemName: String, input: UpdateInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (item.versioned)
            throw IllegalArgumentException("Item [$itemName] is versioned and cannot be updated")

        if (!itemRecDao.existsById(item, input.id))
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found")

        val prevItemRec = itemRecDao.findByIdForWrite(item, input.id)
            ?: throw AccessDeniedException("You are not allowed to update item [$itemName] with ID [${input.id}]")

        if (LIFECYCLE_ATTR_NAME in input.data)
            throw IllegalArgumentException("Lifecycle can be changed only by promote action")

        itemRecDao.lockByIdOrThrow(item, input.id)

        val preparedData = attributeValueHelper.prepareAttributeValues(item, input.data)
        val mergedData = DataHandlerUtil.merge(preparedData, prevItemRec).toMutableMap()
        val filteredData = mergedData.filterKeys { !item.spec.getAttributeOrThrow(it).isCollection() }
        val itemRec = ItemRec(filteredData.toMutableMap())

        // Assign other attributes
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignAuditAttributes(prevItemRec, itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        itemRecDao.updateById(item, input.id, itemRec) // insert

        // Update relations
        relationHelper.updateRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == Type.relation } as Map<String, Any>
        )

        itemRecDao.unlockByIdOrThrow(item, input.id)

        val selectData = itemRec
            .filterKeys { it in selectAttrNames.plus(ID_ATTR_NAME) }
            .toMutableMap()

        return Response(ItemRec(selectData))
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private const val LIFECYCLE_ATTR_NAME = "lifecycle"

        private val logger = LoggerFactory.getLogger(UpdateHandlerImpl::class.java)
    }
}