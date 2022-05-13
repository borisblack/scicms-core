package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.CreateVersionHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.handler.util.RelationHelper
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.data.service.LifecycleManager
import ru.scisolutions.scicmscore.engine.data.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.data.service.PermissionManager
import ru.scisolutions.scicmscore.engine.data.service.SequenceManager
import ru.scisolutions.scicmscore.engine.data.service.VersionManager
import ru.scisolutions.scicmscore.service.ItemService
import java.util.UUID

@Service
class CreateVersionHandlerImpl(
    private val itemService: ItemService,
    private val attributeValueHelper: AttributeValueHelper,
    private val sequenceManager: SequenceManager,
    private val versionManager: VersionManager,
    private val localizationManager: LocalizationManager,
    private val lifecycleManager: LifecycleManager,
    private val permissionManager: PermissionManager,
    private val auditManager: AuditManager,
    private val relationHelper: RelationHelper,
    private val itemRecDao: ItemRecDao,
) : CreateVersionHandler {
    override fun createVersion(itemName: String, input: CreateVersionInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (!item.versioned)
            throw IllegalArgumentException("Item [$itemName] is not versioned")

        if (itemService.findByNameForCreate(item.name) == null)
            throw AccessDeniedException("You are not allowed to create version for item [$itemName]")

        val prevItemRec = itemRecDao.findByIdOrThrow(item, input.id)
        if (prevItemRec.current != true)
            throw IllegalArgumentException("Item [$itemName] with ID [${input.id}] is not a current version")

        if (!item.notLockable)
            itemRecDao.lockByIdOrThrow(item, input.id)

        val preparedData = attributeValueHelper.prepareAttributeValues(item, input.data)
        val filteredData = preparedData.filterKeys { !item.spec.getAttributeOrThrow(it).isCollection() }
        val mergedData = DataHandlerUtil.merge(filteredData, prevItemRec).toMutableMap()
        val itemRec = ItemRec(mergedData).apply {
            id = UUID.randomUUID().toString()
        }

        // Assign other attributes
        sequenceManager.assignSequenceAttributes(item, itemRec)
        versionManager.assignVersionAttributes(item, prevItemRec, itemRec, input.majorRev)
        localizationManager.assignLocaleAttribute(item, itemRec, input.locale)
        lifecycleManager.assignLifecycleAttributes(item, itemRec)
        permissionManager.assignPermissionAttribute(item, itemRec)
        auditManager.assignAuditAttributes(prevItemRec, itemRec)

        DataHandlerUtil.checkRequiredAttributes(item, itemRec.keys)

        // Reset current and lastVersion flags
        itemRecDao.updateById(
            item,
            input.id,
            ItemRec().apply {
                current = false
                lastVersion = false
            }
        )

        itemRecDao.insert(item, itemRec) // insert

        // Update relations
        relationHelper.updateRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == Type.relation } as Map<String, Any>
        )

        // TODO: Maybe copy relations from previous version

        if (!item.notLockable)
            itemRecDao.unlockByIdOrThrow(item, input.id)

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(ItemRec(selectData))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateVersionHandlerImpl::class.java)
    }
}