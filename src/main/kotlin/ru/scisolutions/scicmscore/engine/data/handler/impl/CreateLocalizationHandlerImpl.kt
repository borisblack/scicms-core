package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.CreateLocalizationHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.data.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.data.handler.util.CopyRelationHelper
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.model.CreateLocalizationHook
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.data.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.data.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.data.service.PermissionManager
import ru.scisolutions.scicmscore.engine.data.service.SequenceManager
import ru.scisolutions.scicmscore.service.ClassService
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.util.Maps
import java.util.UUID

@Service
class CreateLocalizationHandlerImpl(
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
) : CreateLocalizationHandler {
    override fun createLocalization(itemName: String, input: CreateLocalizationInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (!item.localized)
            throw IllegalArgumentException("Item [$itemName] is not localized")

        if (itemService.findByNameForCreate(item.name) == null)
            throw AccessDeniedException("You are not allowed to create localization for item [$itemName]")

        if (MAJOR_REV_ATTR_NAME in input.data)
            throw IllegalArgumentException("Major revision can be changed only by createVersion action")

        val prevItemRec = itemRecDao.findByIdOrThrow(item, input.id)
        if (prevItemRec.locale == input.locale)
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] has the same locale (${input.locale})")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id) // lock

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, CreateLocalizationHook::class.java)
        implInstance?.beforeCreateLocalization(itemName, input)

        val preparedData = attributeValueHelper.prepareAttributeValues(item, input.data)
        val filteredData = preparedData
            .filterKeys { !item.spec.getAttributeOrThrow(it).isCollection() }

        val mergedData = Maps.merge(filteredData, prevItemRec).toMutableMap()
        val itemRec = ItemRec(mergedData).apply {
            id = UUID.randomUUID().toString()
        }

        // Assign other attributes
        sequenceManager.assignSequenceAttributes(item, itemRec)
        localizationManager.assignLocaleAttribute(item, itemRec, input.locale)
        lifecycleManager.assignLifecycleAttributes(item, itemRec)
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignUpdateAttributes(itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        itemRecDao.insert(item, itemRec) // insert

        // Update relations
        addRelationHelper.processRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == Type.relation } as Map<String, Any>
        )

        // Copy relations from previous localization
        if (input.copyCollectionRelations == true)
            copyRelationHelper.processCollectionRelations(item, input.id, itemRec.id as String)

        if (!item.notLockable)
            itemRecDao.unlockByIdOrThrow(item, input.id) // unlock

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        val response = Response(ItemRec(selectData))

        implInstance?.afterCreateLocalization(itemName, response)

        return response
    }

    companion object {
        private const val MAJOR_REV_ATTR_NAME = "majorRev"

        private val logger = LoggerFactory.getLogger(CreateLocalizationHandlerImpl::class.java)
    }
}