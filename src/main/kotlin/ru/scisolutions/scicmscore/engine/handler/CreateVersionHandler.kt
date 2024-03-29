package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.CopyRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.CreateVersionHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.service.ClassService
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.engine.service.SequenceManager
import ru.scisolutions.scicmscore.engine.service.VersionManager
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.service.CacheService
import ru.scisolutions.scicmscore.persistence.service.ItemService
import java.util.UUID

@Service
class CreateVersionHandler(
    private val classService: ClassService,
    private val itemService: ItemService,
    private val attributeValueHelper: AttributeValueHelper,
    private val sequenceManager: SequenceManager,
    private val versionManager: VersionManager,
    private val localizationManager: LocalizationManager,
    private val lifecycleManager: LifecycleManager,
    private val permissionManager: PermissionManager,
    private val auditManager: AuditManager,
    private val addRelationHelper: AddRelationHelper,
    private val copyRelationHelper: CopyRelationHelper,
    private val itemRecDao: ItemRecDao,
    private val cacheService: CacheService
) {
    fun createVersion(itemName: String, input: CreateVersionInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (!item.versioned)
            throw IllegalArgumentException("Item [$itemName] is not versioned")

        if (!itemService.canCreate(item.name))
            throw AccessDeniedException("You are not allowed to create version for item [$itemName]")

        val prevItemRec = itemRecDao.findByIdOrThrow(item, input.id)
        if (prevItemRec.current != true)
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] is not a current version")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        val nonCollectionData = input.data.filterKeys { !item.spec.getAttribute(it).isCollection() }
        val mergedData = attributeValueHelper.merge(item, nonCollectionData, prevItemRec)
        val preparedData = attributeValueHelper.prepareValuesToSave(item, mergedData)
        val itemRec = ItemRec(preparedData.toMutableMap()).apply {
            id = UUID.randomUUID().toString()
            lockedBy = null
        }

        // Assign other attributes
        sequenceManager.assignSequenceAttributes(item, itemRec)
        versionManager.assignVersionAttributes(item, prevItemRec, itemRec, input.majorRev)
        localizationManager.assignLocaleAttribute(item, itemRec, input.locale)
        lifecycleManager.assignLifecycleAttributes(item, itemRec)
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignUpdateAttributes(itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        // TODO: Do in one transaction
        // Reset current flag
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
        val implInstance = classService.getCastInstance(item.implementation, CreateVersionHook::class.java)
        implInstance?.beforeCreateVersion(itemName, input, itemRec)

        itemRecDao.insert(item, itemRec) // insert

        // Update relations
        addRelationHelper.processRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttribute(it).type == FieldType.relation } as Map<String, Any>
        )

        // Copy relations from previous version
        if (input.copyCollectionRelations == true)
            copyRelationHelper.processCollectionRelations(item, input.id, itemRec.id as String)

        if (!item.notLockable)
            itemRecDao.unlockByIdOrThrow(item, input.id)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()
        val response = Response(
            ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData))
        )

        implInstance?.afterCreateVersion(itemName, response)

        cacheService.optimizeSchemaCaches(item)

        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateVersionHandler::class.java)
    }
}