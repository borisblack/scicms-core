package ru.scisolutions.scicmscore.engine.handler.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.UpdateHandler
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.UpdateHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.ClassService
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.engine.service.VersionManager
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import ru.scisolutions.scicmscore.util.Maps

@Service
class UpdateHandlerImpl(
    private val classService: ClassService,
    private val itemCache: ItemCache,
    private val attributeValueHelper: AttributeValueHelper,
    private val versionManager: VersionManager,
    private val localizationManager: LocalizationManager,
    private val lifecycleManager: LifecycleManager,
    private val permissionManager: PermissionManager,
    private val auditManager: AuditManager,
    private val addRelationHelper: AddRelationHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao
) : UpdateHandler {
    override fun update(itemName: String, input: UpdateInput, selectAttrNames: Set<String>): Response {
        if (itemName in disabledItemNames)
            throw IllegalArgumentException("Item [$itemName] cannot be updated.")

        val item = itemCache.getOrThrow(itemName)
        if (item.versioned)
            throw IllegalArgumentException("Item [$itemName] is versioned and cannot be updated")

        val prevItemRec = aclItemRecDao.findByIdForWrite(item, input.id)
            ?: throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found")

        if ((itemName == Item.ITEM_TEMPLATE_ITEM_NAME || itemName == Item.ITEM_ITEM_NAME) && prevItemRec[ItemRec.CORE_ATTR_NAME] == true)
            throw IllegalArgumentException("Item [$itemName] cannot be updated.")

        if (STATE_ATTR_NAME in input.data)
            throw IllegalArgumentException("State can be changed only by promote action")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        val nonCollectionData = input.data.filterKeys { !item.spec.getAttributeOrThrow(it).isCollection() }
        val mergedData = Maps.merge(nonCollectionData, prevItemRec).toMutableMap()
        val preparedData = attributeValueHelper.prepareValuesToSave(item, mergedData)
        val itemRec = ItemRec(preparedData.toMutableMap()).apply {
            lockedBy = null
        }

        // Assign other attributes
        versionManager.assignVersionAttributes(item, itemRec, null)
        localizationManager.assignLocaleAttribute(item, itemRec, null)
        lifecycleManager.assignLifecycleAttributes(item, itemRec)
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignUpdateAttributes(itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, UpdateHook::class.java)
        implInstance?.beforeUpdate(itemName, input, itemRec)

        itemRecDao.updateById(item, input.id, itemRec) // update

        // Update relations
        addRelationHelper.processRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == FieldType.relation } as Map<String, Any>
        )

        if (!item.notLockable)
            itemRecDao.unlockByIdOrThrow(item, input.id)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()
        val response = Response(ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData)))

        implInstance?.afterUpdate(itemName, response)

        return response
    }

    companion object {
        private const val ITEM_ITEM_NAME = "item"
        private const val STATE_ATTR_NAME = "state"

        private val disabledItemNames = setOf(ITEM_ITEM_NAME)
    }
}