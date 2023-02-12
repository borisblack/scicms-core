package ru.scisolutions.scicmscore.engine.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.CreateHandler
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.CreateHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.ClassService
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.engine.service.SequenceManager
import ru.scisolutions.scicmscore.engine.service.VersionManager
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import ru.scisolutions.scicmscore.persistence.service.ItemService
import java.util.UUID

@Service
class CreateHandlerImpl(
    private val classService: ClassService,
    private val itemCache: ItemCache,
    private val itemService: ItemService,
    private val attributeValueHelper: AttributeValueHelper,
    private val sequenceManager: SequenceManager,
    private val versionManager: VersionManager,
    private val localizationManager: LocalizationManager,
    private val lifecycleManager: LifecycleManager,
    private val permissionManager: PermissionManager,
    private val auditManager: AuditManager,
    private val addRelationHelper: AddRelationHelper,
    private val itemRecDao: ItemRecDao,
) : CreateHandler {
    override fun create(itemName: String, input: CreateInput, selectAttrNames: Set<String>): Response {
        if (itemName in disabledItemNames)
            throw IllegalArgumentException("Item [$itemName] cannot be created.")

        val item = itemCache.getOrThrow(itemName)
        if (!itemService.canCreate(item.name))
            throw AccessDeniedException("You are not allowed to create item [$itemName]")

        val preparedData = attributeValueHelper.prepareValuesToSave(item, input.data)
        val nonCollectionData = preparedData
            .filterKeys { !item.spec.getAttributeOrThrow(it).isCollection() }
            .toMutableMap()

        val itemRec = ItemRec(nonCollectionData).apply {
            id = UUID.randomUUID().toString()
            configId = id
        }

        // Assign other attributes
        sequenceManager.assignSequenceAttributes(item, itemRec)
        versionManager.assignVersionAttributes(item, itemRec, input.majorRev)
        localizationManager.assignLocaleAttribute(item, itemRec, input.locale)
        lifecycleManager.assignLifecycleAttributes(item, itemRec)
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignAuditAttributes(itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        // Get and call hook
        val implInstance = classService.getCastInstance(item.implementation, CreateHook::class.java)
        implInstance?.beforeCreate(itemName, input, itemRec)

        itemRecDao.insert(item, itemRec) // insert

        addRelationHelper.processRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == FieldType.relation } as Map<String, Any>
        )

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()
        val response = Response(ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData)))

        implInstance?.afterCreate(itemName, response)

        return response
    }

    companion object {
        private const val ITEM_ITEM_NAME = "item"

        private val disabledItemNames = setOf(ITEM_ITEM_NAME)
        private val logger = LoggerFactory.getLogger(CreateHandlerImpl::class.java)
    }
}