package ru.scisolutions.scicmscore.engine.handler.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.Attribute.Type
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.persistence.service.LocationService
import ru.scisolutions.scicmscore.persistence.service.MediaService
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset

@Component
class AttributeValueHelper(
    private val dataProps: DataProps,
    private val itemService: ItemService,
    private val mediaService: MediaService,
    private val locationService: LocationService,
    private val itemRecDao: ItemRecDao
) {
    fun prepareValuesToSave(item: Item, attributes: Map<String, Any?>): Map<String, Any?> {
        val result = attributes
            .filterKeys {
                val attribute = item.spec.getAttributeOrThrow(it)
                !attribute.private && attribute.type != Type.sequence && it !in excludeAttrNames
            }
            .mapValues { (attrName, value) -> prepareValueToSave(item, attrName, value) }

        if (dataProps.trimStrings)
            return result.mapValues { (_, value) -> if (value is String) value.trim() else value }

        return result
    }

    fun prepareValueToSave(item: Item, attrName: String, value: Any?): Any? {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        if (value == null) {
            if (attribute.required)
                throw IllegalArgumentException("Item [${item.name}], attribute [${attrName}]: Value is required")

            return null
        }

        validateAttributeValue(item, attrName, attribute, value)

        return when (attribute.type) {
            Type.uuid, Type.string, Type.text, Type.enum, Type.email -> value
            Type.sequence -> throw IllegalArgumentException("Sequence cannot be set manually")
            Type.password -> passwordEncoder.encode(value as String).toString()
            Type.int, Type.long, Type.float, Type.double, Type.decimal -> value
            Type.date, Type.time, Type.datetime, Type.timestamp -> value
            Type.bool -> value
            Type.array, Type.json -> objectMapper.writeValueAsString(value)
            Type.media, Type.location -> value
            Type.relation -> if (attribute.isCollection()) (value as List<*>).toSet() else value
        }
    }

    private fun validateAttributeValue(item: Item, attrName: String, attribute: Attribute, value: Any) {
        when (attribute.type) {
            Type.uuid -> {}
            Type.string, Type.text, Type.enum, Type.email, Type.password, Type.media, Type.location -> {
                if (value !is String)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                when (attribute.type) {
                    Type.string -> {
                        if (attribute.length == null)
                            throw IllegalArgumentException("The length is required for the string type")

                        if (value.length > attribute.length)
                            throw IllegalArgumentException("The string length exceeds maximum")

                        if (!attribute.pattern.isNullOrBlank()) {
                            val regex = attribute.pattern.toRegex()
                            if (regex.matches(value))
                                throw IllegalArgumentException("The string [$value] does not match pattern [${attribute.pattern}]")
                        }
                    }
                    Type.text -> {}
                    Type.enum -> {
                        if (attribute.enumSet == null)
                            throw IllegalArgumentException("enumSet is required for the enum type")

                        if (value !in attribute.enumSet)
                            throw IllegalArgumentException("Enumeration set does not contain value [$value]. Possible values: ${attribute.enumSet.joinToString()}")
                    }
                    Type.email -> {
                        if (!simpleEmailRegex.matches(value))
                            throw IllegalArgumentException("The string [$value] does not match the email pattern")
                    }
                    Type.password -> {
                        if (value.isBlank())
                            throw IllegalArgumentException("Password string cannot be blank")
                    }
                    Type.media -> {
                        if (!mediaService.existsById(value))
                            throw IllegalArgumentException("Media with ID [$value] does not exist")
                    }
                    Type.location -> {
                        if (!locationService.existsById(value))
                            throw IllegalArgumentException("Location with ID [$value] does not exist")
                    }
                    else -> throw IllegalArgumentException("Unsupported attribute type")
                }
            }
            Type.sequence -> throw IllegalArgumentException("Sequence cannot be set manually")
            Type.int -> {
                if (value !is Int)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateAttributeNumberValue(item, attrName, attribute, value)
            }
            Type.long -> {
                if (value !is Int && value !is Long)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateAttributeNumberValue(item, attrName, attribute, value as Number)
            }
            Type.float -> {
                if (value !is Float)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateAttributeNumberValue(item, attrName, attribute, value)
            }
            Type.double -> {
                if (value !is Float && value !is Double)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateAttributeNumberValue(item, attrName, attribute, value as Number)
            }
            Type.decimal -> {
                if (value !is Float && value !is Double && value !is BigDecimal)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateAttributeNumberValue(item, attrName, attribute, value as Number)
            }
            Type.date -> {
                if (value !is LocalDate)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
            }
            Type.time -> {
                if (value !is OffsetTime)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
            }
            Type.datetime -> {
                if (value !is OffsetDateTime)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
            }
            Type.timestamp -> {
                if (value !is OffsetDateTime)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
            }
            Type.bool -> {
                if (value !is Boolean)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
            }
            Type.array -> {
                if (value !is List<*>)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
            }
            Type.json -> {
                if (value !is Map<*, *>)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
            }
            Type.relation -> {
                val targetItem = itemService.getByName(requireNotNull(attribute.target))
                if (attribute.isCollection()) {
                    if (value !is List<*>)
                        throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                    val ids = value.filterIsInstance<String>()
                    if (ids.size != value.size)
                        throw IllegalArgumentException("Wrong type of item [${item.name}] IDs")

                    if (!itemRecDao.existAllByIds(targetItem, ids.toSet()))
                        throw IllegalArgumentException("Items [${attribute.target}] with IDs [${ids.joinToString()}] do not exist")
                } else {
                    if (value !is String)
                        throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                    if (!itemRecDao.existsById(targetItem, value))
                        throw IllegalArgumentException("Item [${attribute.target}] with ID [$value] does not exist")
                }
            }
        }
    }

    private fun validateAttributeNumberValue(item: Item, attrName: String, attribute: Attribute, value: Number) {
        when (value) {
            is Int -> {
                if (attribute.minRange != null && value < attribute.minRange)
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))

                if (attribute.maxRange != null && value > attribute.maxRange)
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
            }
            is Long -> {
                if (attribute.minRange != null && value < attribute.minRange)
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))

                if (attribute.maxRange != null && value > attribute.maxRange)
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
            }
            is Float -> {
                if (attribute.minRange != null && value < attribute.minRange)
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))

                if (attribute.maxRange != null && value > attribute.maxRange)
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
            }
            is Double -> {
                if (attribute.minRange != null && value < attribute.minRange)
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))

                if (attribute.maxRange != null && value > attribute.maxRange)
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
            }
            is BigDecimal -> {
                if (attribute.minRange != null && value < attribute.minRange.toBigDecimal())
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))

                if (attribute.maxRange != null && value > attribute.maxRange.toBigDecimal())
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
            }
        }
    }

    fun prepareValuesToReturn(item: Item, attributes: Map<String, Any?>): MutableMap<String, Any?> =
        attributes
            .mapValues { (attrName, value) -> prepareValueToReturn(item, attrName, value) }
            .toMutableMap()

    fun prepareValueToReturn(item: Item, attrName: String, value: Any?): Any? {
        val attribute = item.spec.getAttributeOrThrow(attrName)

        return when (attribute.type) {
            Type.uuid, Type.string, Type.text, Type.enum, Type.email, Type.sequence -> value
            Type.password -> ItemRec.PASSWORD_PLACEHOLDER
            Type.int, Type.long, Type.float, Type.double, Type.decimal -> value
            Type.date -> value
            Type.time -> if (value is OffsetTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value
            Type.datetime, Type.timestamp -> if (value is OffsetDateTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value
            Type.bool -> value
            Type.array, Type.json -> if (value == null) null else objectMapper.readValue(value as String, Map::class.java)
            Type.media, Type.location, Type.relation -> value
        }
    }

    companion object {
        private const val WRONG_VALUE_TYPE_MSG = "Item [%s], attribute [%s], value [%s]: Wrong value type"
        private const val VALUE_LESS_THAN_MIN_MSG = "Item [%s], attribute [%s], value [%s]: The value is less than minRange"
        private const val VALUE_MORE_THAN_MAX_MSG = "Item [%s], attribute [%s], value [%s]: The value is more than maxRange"
        private const val MAJOR_REV_ATTR_NAME = "majorRev"
        private const val LOCALE_ATTR_NAME = "locale"
        private const val STATE_ATTR_NAME = "state"

        private val excludeAttrNames = setOf(MAJOR_REV_ATTR_NAME, LOCALE_ATTR_NAME, STATE_ATTR_NAME)
        private val simpleEmailRegex = Regex("\\w+@\\w+\\.\\w+")
        private val passwordEncoder = BCryptPasswordEncoder()
        private val objectMapper = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}