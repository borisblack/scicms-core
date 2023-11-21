package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.CopyRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.CreateLocalizationHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.ClassService
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.engine.service.SequenceManager
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import ru.scisolutions.scicmscore.persistence.service.ItemService
import java.util.UUID

@Service
class CreateLocalizationHandler(
    private val classService: ClassService,
    private val itemCache: ItemCache,
    private val itemService: ItemService,
    private val attributeValueHelper: AttributeValueHelper,
    private val sequenceManager: SequenceManager,
    private val localizationManager: LocalizationManager,
    private val lifecycleManager: LifecycleManager,
    private val permissionManager: PermissionManager,
    private val auditManager: AuditManager,
    private val addRelationHelper: AddRelationHelper,
    private val copyRelationHelper: CopyRelationHelper,
    private val itemRecDao: ItemRecDao,
) {
    fun createLocalization(itemName: String, input: CreateLocalizationInput, selectAttrNames: Set<String>): Response {
        val item = itemCache.getOrThrow(itemName)
        if (!item.localized)
            throw IllegalArgumentException("Item [$itemName] is not localized")

        if (!itemService.canCreate(item.name))
            throw AccessDeniedException("You are not allowed to create localization for item [$itemName]")

        if (MAJOR_REV_ATTR_NAME in input.data)
            throw IllegalArgumentException("Major revision can be changed only by createVersion action")

        val prevItemRec = itemRecDao.findByIdOrThrow(item, input.id)
        if (prevItemRec.locale == input.locale)
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] has the same locale (${input.locale})")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id) // lock

        val nonCollectionData = input.data.filterKeys { !item.spec.getAttributeOrThrow(it).isCollection() }
        val mergedData = attributeValueHelper.merge(item, nonCollectionData, prevItemRec)
        val preparedData = attributeValueHelper.prepareValuesToSave(item, mergedData)
        val itemRec = ItemRec(preparedData.toMutableMap()).apply {
            id = UUID.randomUUID().toString()
            lockedBy = null
        }

        // Assign other attributes
        sequenceManager.assignSequenceAttributes(item, itemRec)
        localizationManager.assignLocaleAttribute(item, itemRec, input.locale)
        lifecycleManager.assignLifecycleAttributes(item, itemRec)
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignUpdateAttributes(itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        // TODO: Do in one transaction
        // Reset current flag
        if (item.versioned && itemRec.current == true)
            itemRecDao.updateByAttributes(
                item = item,
                whereAttributes = mapOf(
                    ItemRec.CONFIG_ID_ATTR_NAME to requireNotNull(itemRec.configId),
                    ItemRec.LOCALE_ATTR_NAME to itemRec.locale
                ),
                updateAttributes = mapOf(
                    ItemRec.CURRENT_ATTR_NAME to false
                )
            )

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, CreateLocalizationHook::class.java)
        implInstance?.beforeCreateLocalization(itemName, input, itemRec)

        itemRecDao.insert(item, itemRec) // insert

        // Update relations
        addRelationHelper.processRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == FieldType.relation } as Map<String, Any>
        )

        // Copy relations from previous localization
        if (input.copyCollectionRelations == true)
            copyRelationHelper.processCollectionRelations(item, input.id, itemRec.id as String)

        if (!item.notLockable)
            itemRecDao.unlockByIdOrThrow(item, input.id) // unlock

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()
        val response = Response(ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData)))

        implInstance?.afterCreateLocalization(itemName, response)

        return response
    }

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"

        private val logger = LoggerFactory.getLogger(CreateLocalizationHandler::class.java)
    }
}