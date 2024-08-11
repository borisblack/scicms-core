package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.util.AddRelationHelper
import ru.scisolutions.scicmscore.engine.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.hook.CreateHook
import ru.scisolutions.scicmscore.engine.hook.GenerateIdHook
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
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
import ru.scisolutions.scicmscore.engine.service.VersionManager
import ru.scisolutions.scicmscore.service.ClassService

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
    private val cacheService: CacheService,
    private val idGenerator: DefaultIdGenerator,
) {
    fun create(itemName: String, input: CreateInput, selectAttrNames: Set<String>): Response {
        if (itemName in disabledItemNames) {
            throw IllegalArgumentException("Item [$itemName] cannot be created.")
        }

        val item = itemService.getByName(itemName)
        if (!itemService.canCreate(item.name)) {
            throw AccessDeniedException("You are not allowed to create item [$itemName]")
        }

        val nonCollectionData = input.data.filterKeys { !item.spec.getAttribute(it).isCollection() }
        val preparedData = attributeValueHelper.prepareValuesToSave(item, nonCollectionData)
        val itemRec =
            ItemRec(preparedData.toMutableMap()).apply {
                val generateIdHook = classService.getCastInstance(item.implementation, GenerateIdHook::class.java)
                val id = generateIdHook?.generateId(itemName) ?: idGenerator.generateId()
                if (this[item.idAttribute] == null) {
                    this[item.idAttribute] = id
                }

                if (item.hasIdAttribute()) {
                    this.id = id
                }

                if (item.hasConfigIdAttribute()) {
                    this.configId = id
                }
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
        val createHook = classService.getCastInstance(item.implementation, CreateHook::class.java)
        val preCreatedItemRec = createHook?.beforeCreate(itemName, input, itemRec)
        if (preCreatedItemRec == null) {
            itemRecDao.insert(item, itemRec) // insert

            addRelationHelper.addRelations(
                item,
                itemRec,
                itemRec.filterKeys { item.spec.getAttribute(it).type == FieldType.relation } as Map<String, Any>,
            )
        }

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = (preCreatedItemRec ?: itemRec).filterKeys { it in attrNames }.toMutableMap()
        val response =
            Response(
                ItemRec(attributeValueHelper.prepareValuesToReturn(item, selectData)),
            )

        createHook?.afterCreate(itemName, response)

        cacheService.optimizeSchemaCaches(item)

        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateHandler::class.java)
        private val disabledItemNames: Set<String> = setOf(
            // Item.ITEM_ITEM_NAME
        )
    }
}
