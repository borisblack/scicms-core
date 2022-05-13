package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.CreateHandler
import ru.scisolutions.scicmscore.engine.data.handler.util.AttributeValueHelper
import ru.scisolutions.scicmscore.engine.data.handler.util.DataHandlerUtil
import ru.scisolutions.scicmscore.engine.data.handler.util.RelationHelper
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.CreateInput
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
class CreateHandlerImpl(
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
) : CreateHandler {
    override fun create(itemName: String, input: CreateInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (itemService.findByNameForCreate(item.name) == null)
            throw AccessDeniedException("You are not allowed to create item [$itemName]")

        val preparedData = attributeValueHelper.prepareAttributeValues(item, input.data)
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

        itemRecDao.insert(item, itemRec) // insert

        relationHelper.updateRelations(
            item,
            itemRec.id as String,
            preparedData.filterKeys { item.spec.getAttributeOrThrow(it).type == Type.relation } as Map<String, Any>
        )

        val attrNames = DataHandlerUtil.prepareSelectedAttrNames(item, selectAttrNames)
        val selectData = itemRec.filterKeys { it in attrNames }.toMutableMap()

        return Response(ItemRec(selectData))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateHandlerImpl::class.java)
    }
}