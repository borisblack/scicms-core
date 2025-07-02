package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.CopyRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.CreateLocalizationHook
import ru.scisolutions.scicmscore.engine.hook.GenerateIdHook
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.service.CacheService
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.DefaultIdGenerator
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.engine.service.SequenceManager
import ru.scisolutions.scicmscore.service.ClassService

@Service
class CreateLocalizationHandler(
    private val classService: ClassService,
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
    private val cacheService: CacheService,
    private val idGenerator: DefaultIdGenerator
) {
    fun createLocalization(itemName: String, input: CreateLocalizationInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (!item.localized || !item.hasLocaleAttribute()) {
            throw IllegalArgumentException("Item [$itemName] is not localized")
        }

        if (!itemService.canCreate(item.name)) {
            throw AccessDeniedException("You are not allowed to create localization for item [$itemName]")
        }

        if (MAJOR_REV_ATTR_NAME in input.data) {
            throw IllegalArgumentException("Major revision can be changed only by createVersion action")
        }

        val prevItemRec = itemRecDao.findByIdOrThrow(item, input.id)
        if (prevItemRec.locale == input.locale) {
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] has the same locale (${input.locale})")
        }

        val isLockable = !item.notLockable && item.hasLockedByAttribute()
        if (isLockable) {
            itemRecDao.lockByIdOrThrow(item, input.id) // lock
        }

        val nonCollectionData = input.data.filterKeys { !item.spec.getAttribute(it).isCollection() }
        val mergedData = attributeValueHelper.merge(item, nonCollectionData, prevItemRec)
        val preparedData = attributeValueHelper.prepareValuesToSave(item, mergedData)
        val itemRec =
            ItemRec(preparedData.toMutableMap().withDefault { null }).apply {
                val generateIdHook = classService.getCastInstance(item.implementation, GenerateIdHook::class.java)
                val id = generateIdHook?.generateId(itemName) ?: idGenerator.generateId()
                if (this[item.idAttribute] == null) {
                    this[item.idAttribute] = id
                }

                if (item.hasIdAttribute() && this.id == null) {
                    this.id = id
                }
            }
        if (isLockable) {
            itemRec.lockedBy = null
        }

        // Assign other attributes
        sequenceManager.assignSequenceAttributes(item, itemRec)
        localizationManager.assignLocaleAttribute(item, itemRec, input.locale)
        lifecycleManager.assignLifecycleAttributes(item, itemRec)
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignUpdateAttributes(item, itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        // TODO: Do in one transaction
        // Reset current flag
        if (item.versioned && itemRec.current == true) {
            itemRecDao.updateByAttributes(
                item = item,
                whereAttributes =
                mapOf(
                    ItemRec.CONFIG_ID_ATTR_NAME to requireNotNull(itemRec.configId),
                    ItemRec.LOCALE_ATTR_NAME to itemRec.locale
                ),
                updateAttributes =
                mapOf(
                    ItemRec.CURRENT_ATTR_NAME to false
                )
            )
        }

        // Get and call hook
        val createLocalizationHook = classService.getCastInstance(item.implementation, CreateLocalizationHook::class.java)
        createLocalizationHook?.beforeCreateLocalization(itemName, input, itemRec)

        itemRecDao.insert(item, itemRec) // insert

        // Update relations
        addRelationHelper.addRelations(
            item,
            itemRec,
            preparedData.filterKeys { item.spec.getAttribute(it).type == FieldType.relation } as Map<String, Any>
        )

        // Copy relations from previous localization
        if (input.copyCollectionRelations == true) {
            copyRelationHelper.copyCollectionRelations(item, prevItemRec, itemRec)
        }

        if (isLockable) {
            itemRecDao.unlockByIdOrThrow(item, input.id) // unlock
        }

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()
        val response =
            Response(
                ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData))
            )

        createLocalizationHook?.afterCreateLocalization(itemName, response)

        cacheService.actualizeCaches(item, itemRec)

        return response
    }

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"

        private val logger = LoggerFactory.getLogger(CreateLocalizationHandler::class.java)
    }
}
