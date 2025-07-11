package ru.scisolutions.scicmscore.engine.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.UpdateHook
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.CacheService
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.engine.service.VersionManager
import ru.scisolutions.scicmscore.service.ClassService

@Service
class UpdateHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val attributeValueHelper: AttributeValueHelper,
    private val versionManager: VersionManager,
    private val localizationManager: LocalizationManager,
    private val lifecycleManager: LifecycleManager,
    private val permissionManager: PermissionManager,
    private val auditManager: AuditManager,
    private val addRelationHelper: AddRelationHelper,
    private val itemRecDao: ItemRecDao,
    private val aclItemRecDao: ACLItemRecDao,
    private val cacheService: CacheService
) {
    fun update(itemName: String, input: UpdateInput, selectAttrNames: Set<String>): Response {
        if (itemName in disabledItemNames) {
            throw IllegalArgumentException("Item [$itemName] cannot be updated.")
        }

        val item = itemService.getByName(itemName)
        if (item.versioned) {
            throw IllegalArgumentException("Item [$itemName] is versioned and cannot be updated")
        }

        val prevItemRec =
            aclItemRecDao.findByIdForWrite(item, input.id)
                ?: throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] not found")

        if ((itemName == Item.ITEM_TEMPLATE_ITEM_NAME || itemName == Item.ITEM_ITEM_NAME) && prevItemRec[ItemRec.CORE_ATTR_NAME] == true) {
            throw IllegalArgumentException("Item [$itemName] cannot be updated.")
        }

        if (STATE_ATTR_NAME in input.data) {
            throw IllegalArgumentException("State can be changed only by promote action")
        }

        val isLockable = !item.notLockable && item.hasLockedByAttribute()
        if (isLockable) {
            itemRecDao.lockByIdOrThrow(item, input.id)
        }

        val nonCollectionData = input.data.filterKeys { !item.spec.getAttribute(it).isCollection() }
        val mergedData = attributeValueHelper.merge(item, nonCollectionData, prevItemRec)
        val preparedData = attributeValueHelper.prepareValuesToSave(item, mergedData)
        val itemRec = ItemRec(preparedData.toMutableMap().withDefault { null })
        if (isLockable) {
            itemRec.lockedBy = null
        }

        // Assign other attributes
        versionManager.assignVersionAttributes(item, itemRec, null)
        localizationManager.assignLocaleAttribute(item, itemRec, null)
        lifecycleManager.assignLifecycleAttributes(item, prevItemRec, itemRec)
        permissionManager.assignPermissionAttribute(item, prevItemRec, itemRec)
        auditManager.assignUpdateAttributes(item, itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, UpdateHook::class.java)
        val preUpdatedItemRec = implInstance?.beforeUpdate(itemName, input, itemRec)
        if (preUpdatedItemRec == null) {
            itemRecDao.updateById(item, input.id, itemRec) // update

            // Update relations
            addRelationHelper.addRelations(
                item,
                itemRec,
                itemRec.filterKeys { item.spec.getAttribute(it).type == FieldType.relation } as Map<String, Any>
            )
        }

        if (isLockable) {
            itemRecDao.unlockByIdOrThrow(item, input.id)
        }

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = (preUpdatedItemRec ?: itemRec).filterKeys { it in attrNames }.toMutableMap()
        val response =
            Response(
                ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData))
            )

        implInstance?.afterUpdate(itemName, response)

        cacheService.actualizeCaches(item, itemRec)

        return response
    }

    companion object {
        private const val STATE_ATTR_NAME = "state"

        private val disabledItemNames: Set<String> = setOf(
            // Item.ITEM_ITEM_NAME
        )
    }
}
