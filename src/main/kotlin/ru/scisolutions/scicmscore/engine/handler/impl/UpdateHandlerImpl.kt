package ru.scisolutions.scicmscore.engine.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.UpdateHandler
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.UpdateHook
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.ClassService
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.model.Attribute.Type
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.util.Maps
import java.util.UUID

@Service
class UpdateHandlerImpl(
    private val classService: ClassService,
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

        if (STATE_ATTR_NAME in input.data)
            throw IllegalArgumentException("State can be changed only by promote action")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, UpdateHook::class.java)
        implInstance?.beforeUpdate(itemName, input)

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
            itemRec.id as UUID,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == Type.relation } as Map<String, Any>
        )

        if (!item.notLockable)
            itemRecDao.unlockByIdOrThrow(item, input.id)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        val response = Response(ItemRec(selectData))

        implInstance?.afterUpdate(itemName, response)

        return response
    }

    companion object {
        private const val ITEM_ITEM_NAME = "item"
        private const val STATE_ATTR_NAME = "state"

        private val disabledItemNames = setOf(ITEM_ITEM_NAME)
        private val logger = LoggerFactory.getLogger(UpdateHandlerImpl::class.java)
    }
}