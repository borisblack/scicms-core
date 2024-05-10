package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.CreateHook
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.service.ClassService
import ru.scisolutions.scicmscore.engine.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import ru.scisolutions.scicmscore.engine.service.SequenceManager
import ru.scisolutions.scicmscore.engine.service.VersionManager
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.CacheService
import ru.scisolutions.scicmscore.persistence.service.ItemService
import java.util.UUID

@Service
class CreateHandler(
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
    private val itemRecDao: ItemRecDao,
    private val cacheService: CacheService
) {
    fun create(itemName: String, input: CreateInput, selectAttrNames: Set<String>): Response {
        if (itemName in disabledItemNames)
            throw IllegalArgumentException("Item [$itemName] cannot be created.")

        val item = itemService.getByName(itemName)
        if (!itemService.canCreate(item.name))
            throw AccessDeniedException("You are not allowed to create item [$itemName]")

        val nonCollectionData = input.data.filterKeys { !item.spec.getAttribute(it).isCollection() }
        val preparedData = attributeValueHelper.prepareValuesToSave(item, nonCollectionData)
        val itemRec = ItemRec(preparedData.toMutableMap()).apply {
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

        val hookItemRec = implInstance?.create(itemName, input, itemRec)
        if (hookItemRec == null) {
            itemRecDao.insert(item, itemRec) // insert

            addRelationHelper.processRelations(
                item,
                itemRec.id as String,
                itemRec.filterKeys { item.spec.getAttribute(it).type == FieldType.relation } as Map<String, Any>
            )
        }

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = (hookItemRec ?: itemRec).filterKeys { it in attrNames }.toMutableMap()
        val response = Response(
            ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData))
        )

        implInstance?.afterCreate(itemName, response)

        cacheService.optimizeSchemaCaches(item)

        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateHandler::class.java)
        private val disabledItemNames = setOf(Item.ITEM_ITEM_NAME)
    }
}