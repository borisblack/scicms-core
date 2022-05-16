package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.UpdateHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.data.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.data.service.PermissionManager
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.util.Maps

@Service
class UpdateHandlerImpl(
    private val itemService: ItemService,
    private val attributeValueHelper: AttributeValueHelper,
    private val permissionManager: PermissionManager,
    private val auditManager: AuditManager,
    private val addRelationHelper: AddRelationHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) : UpdateHandler {
    override fun update(itemName: String, input: UpdateInput, selectAttrNames: Set<String>): Response {
        if (itemName in disabledItemNames)
            throw IllegalArgumentException("Item [$itemName] cannot be updated.")

        val item = itemService.getByName(itemName)
        if (item.versioned)
            throw IllegalArgumentException("Item [$itemName] is versioned and cannot be updated")

        val prevItemRec = aclItemRecDao.findByIdForWrite(item, input.id)
            ?: throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found")

        if (LIFECYCLE_ATTR_NAME in input.data)
            throw IllegalArgumentException("Lifecycle can be changed only by promote action")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        val preparedData = attributeValueHelper.prepareAttributeValues(item, input.data)
        val mergedData = Maps.merge(preparedData, prevItemRec).toMutableMap()
        val filteredData = mergedData.filterKeys { !item.spec.getAttributeOrThrow(it).isCollection() }
        val itemRec = ItemRec(filteredData.toMutableMap())

        // Assign other attributes
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignUpdateAttributes(itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        itemRecDao.updateById(item, input.id, itemRec) // update

        // Update relations
        addRelationHelper.processRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == Type.relation } as Map<String, Any>
        )

        if (!item.notLockable)
            itemRecDao.unlockByIdOrThrow(item, input.id)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(ItemRec(selectData))
    }

    companion object {
        private const val ITEM_ITEM_NAME = "item"
        private const val LIFECYCLE_ATTR_NAME = "lifecycle"

        private val disabledItemNames = setOf(ITEM_ITEM_NAME)
        private val logger = LoggerFactory.getLogger(UpdateHandlerImpl::class.java)
    }
}