package ru.scisolutions.scicmscore.engine.data.handler.impl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.handler.CreateHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.ItemInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.data.service.LocalizationManager
import ru.scisolutions.scicmscore.engine.data.service.SequenceManager
import ru.scisolutions.scicmscore.engine.data.service.VersionManager
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.ManyToManyUnidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToManyInversedBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.model.relation.OneToOneBidirectionalRelation
import ru.scisolutions.scicmscore.engine.schema.service.RelationManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import java.util.UUID

@Service
class CreateHandlerImpl(
    private val itemService: ItemService,
    private val attributeValueValidator: AttributeValueValidator,
    private val auditManager: AuditManager,
    private val versionManager: VersionManager,
    private val localizationManager: LocalizationManager,
    private val sequenceManager: SequenceManager,
    private val relationManager: RelationManager,
    private val itemRecDao: ItemRecDao,
) : CreateHandler {
    override fun create(itemName: String, input: ItemInput, selectAttrNames: Set<String>): Response {
        val item = itemService.getByName(itemName)
        if (itemService.findByNameForCreate(item.name) == null)
            throw AccessDeniedException("You are not allowed to create item [$itemName]")

        val preparedData = prepareData(item, input.data)
        val nonCollectionData = preparedData.filter { (attrName, _) -> !item.spec.getAttributeOrThrow(attrName).isCollection() }
        val itemRec = ItemRec(nonCollectionData.toMutableMap()).apply {
            id = UUID.randomUUID().toString()
            configId = id
            createdAt = null
            createdBy = null
        }

        sequenceManager.assignSequenceAttributes(item, itemRec)
        auditManager.assignAuditAttributes(itemRec)
        versionManager.assignVersionAttributes(item, itemRec, input.majorRev)
        localizationManager.assignLocaleAttribute(item, itemRec, input.locale)

        checkRequiredAttributes(item, itemRec)

        itemRecDao.insert(item, itemRec) // insert

        updateRelations(
            item,
            itemRec.id as String,
            preparedData.filter { (attrName, _) -> item.spec.getAttributeOrThrow(attrName).type == Type.relation } as Map<String, Any>
        )

        val selectData = itemRec
            .filter { (attrName, _) -> attrName in selectAttrNames.plus(ID_ATTR_NAME) }
            .toMutableMap()

        return Response(ItemRec(selectData))
    }

    private fun prepareData(item: Item, data: Map<String, Any?>): Map<String, Any?> =
        data.asSequence()
            .filter { (attrName, _) ->
                val attribute = item.spec.getAttributeOrThrow(attrName)
                !attribute.private && attribute.type != Type.sequence
            }
            // .map { (attrName, value) -> attrName to (if (value is String) value.trim() else value) }
            .map { (attrName, value) -> attrName to prepareAttribute(item, attrName, value as Any) }
            .toMap()

    private fun prepareAttribute(item: Item, attrName: String, value: Any?): Any? {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        if (value == null) {
            if (attribute.required)
                throw IllegalArgumentException("Item [${item.name}], attribute [${attrName}]: Value is required")

            return null
        }

        attributeValueValidator.validate(item, attrName, attribute, value)

        return when (attribute.type) {
            Type.uuid, Type.string, Type.text, Type.enum, Type.email -> value
            Type.sequence -> throw IllegalArgumentException("Sequence cannot be set manually")
            Type.password -> passwordEncoder.encode(value as String).toString()
            Type.int, Type.long, Type.float, Type.double, Type.decimal -> value
            Type.date, Type.time, Type.datetime, Type.timestamp -> value
            Type.bool -> value
            Type.array, Type.json -> objectMapper.writeValueAsString(value)
            Type.media -> value
            Type.relation -> if (attribute.isCollection()) (value as List<*>).toSet() else value
            else -> throw IllegalArgumentException("Unsupported attribute type")
        }
    }

    private fun checkRequiredAttributes(item: Item, itemRec: ItemRec) {
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> attribute.required }
            .forEach { (attrName, _) ->
                if (attrName !in itemRec)
                    throw IllegalArgumentException("Attribute [$attrName] is required")
            }
    }

    private fun updateRelations(item: Item, itemRecId: String, relations: Map<String, Any>) {
        relations.forEach { (attrName, value) ->
            updateRelation(item, itemRecId, attrName, value)
        }
    }

    private fun updateRelation(item: Item, itemRecId: String, relAttrName: String, relAttrValue: Any) {
        val attribute = item.spec.getAttributeOrThrow(relAttrName)
        when (val relation = relationManager.getAttributeRelation(item, relAttrName, attribute)) {
            is OneToOneBidirectionalRelation -> {
                if (relation.isOwning) {
                    val inversedItemRec = ItemRec(mutableMapOf(relation.inversedAttrName to itemRecId))
                    itemRecDao.updateById(relation.inversedItem, relAttrValue as String, inversedItemRec)
                } else {
                    val owningItemRec = ItemRec(mutableMapOf(relation.owningAttrName to itemRecId))
                    itemRecDao.updateById(relation.owningItem, relAttrValue as String, owningItemRec)
                }
            }
            is OneToManyInversedBidirectionalRelation -> {
                val inversedItemRec = ItemRec(mutableMapOf(relation.inversedAttrName to itemRecId))
                itemRecDao.updateById(relation.inversedItem, relAttrValue as String, inversedItemRec)
            }
            is ManyToManyRelation -> {
                relAttrValue as List<*>
                when (relation) {
                    is ManyToManyUnidirectionalRelation -> {
                        relAttrValue.forEach { addManyToManyRelation(relation.intermediateItem.name, itemRecId, it as String) }
                    }
                    is ManyToManyBidirectionalRelation -> {
                        if (relation.isOwning)
                            relAttrValue.forEach { addManyToManyRelation(relation.intermediateItem.name, itemRecId, it as String) }
                        else
                            relAttrValue.forEach { addManyToManyRelation(relation.intermediateItem.name, it as String, itemRecId) }
                    }
                }
            }
        }
    }

    private fun addManyToManyRelation(itemName: String, sourceId: String, targetId: String) {
        val intermediateMap = mapOf(
            INTERMEDIATE_SOURCE_ATTR_NAME to sourceId,
            INTERMEDIATE_TARGET_ATTR_NAME to targetId
        )
        create(itemName, ItemInput(intermediateMap), emptySet()) // recursive call
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private const val INTERMEDIATE_SOURCE_ATTR_NAME = "source"
        private const val INTERMEDIATE_TARGET_ATTR_NAME = "target"

        private val logger = LoggerFactory.getLogger(CreateHandlerImpl::class.java)
        private val passwordEncoder = BCryptPasswordEncoder()

        private val objectMapper = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}