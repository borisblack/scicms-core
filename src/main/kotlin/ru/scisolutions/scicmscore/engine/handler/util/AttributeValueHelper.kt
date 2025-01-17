package ru.scisolutions.scicmscore.engine.handler.util

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.persistence.service.MediaService
import ru.scisolutions.scicmscore.security.service.impl.UserGroupManagerImpl
import ru.scisolutions.scicmscore.util.Json
import ru.scisolutions.scicmscore.util.Maps
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
    private val itemRecDao: ItemRecDao
) {
    fun merge(item: Item, from: Map<String, Any?>, to: ItemRec): Map<String, Any?> {
        val filteredFrom =
            from.filter { (k, v) ->
                val attribute = item.spec.getAttribute(k)
                !(attribute.type == FieldType.password && v == ItemRec.PASSWORD_PLACEHOLDER)
            }

        return Maps.merge(filteredFrom, to)
    }

    fun prepareValuesToSave(item: Item, values: Map<String, Any?>): Map<String, Any?> {
        val map =
            values
                .filterKeys {
                    val attribute = item.spec.getAttribute(it)
                    !attribute.private && attribute.type != FieldType.sequence && it !in excludeAttrNames
                }
                .toMutableMap()

        // Set default values for required attributes
        val requiredAttributesWithDefaultValue =
            item.spec.attributes
                .filterKeys { it !in excludeAttrNames }
                .filterValues {
                    !it.private && it.type != FieldType.sequence && it.required && it.defaultValue != null
                }

        requiredAttributesWithDefaultValue.forEach { (attrName, attr) ->
            if (map[attrName] == null) {
                map[attrName] = attr.parseDefaultValue()
            }
        }

        val result = map.mapValues { (attrName, value) -> prepareValueToSave(item, attrName, value) }

        if (dataProps.trimStrings) {
            return result.mapValues { (_, value) -> if (value is String) value.trim() else value }
        }

        return result
    }

    fun prepareValueToSave(item: Item, attrName: String, value: Any?): Any? {
        val attribute = item.spec.getAttribute(attrName)
        if (value == null || (value is String && value.isBlank())) {
            if (attribute.defaultValue !== null) {
                return attribute.parseDefaultValue()
            }

            if (attribute.required) {
                throw IllegalArgumentException("Item [${item.name}], attribute [$attrName]: Value is required")
            }

            return null
        }

        validateAttributeValue(item, attrName, attribute, value)

        return when (attribute.type) {
            FieldType.uuid, FieldType.string, FieldType.text, FieldType.enum, FieldType.email -> value
            FieldType.sequence -> throw IllegalArgumentException("Sequence cannot be set manually")
            FieldType.password -> if (attribute.encode == true) UserGroupManagerImpl.passwordEncoder.encode(value as String) else value
            FieldType.int -> if (value is String) value.toInt() else value
            FieldType.long -> if (value is String) value.toLong() else value
            FieldType.float -> if (value is String) value.toFloat() else value
            FieldType.double -> if (value is String) value.toDouble() else value
            FieldType.decimal -> if (value is String) value.toBigDecimal() else value
            FieldType.date, FieldType.time, FieldType.datetime, FieldType.timestamp -> value
            FieldType.bool -> value
            FieldType.array, FieldType.json -> value
            FieldType.media -> value
            FieldType.relation -> if (attribute.isCollection()) (value as List<*>).toSet() else value
        }
    }

    private fun validateAttributeValue(item: Item, attrName: String, attribute: Attribute, value: Any) {
        when (attribute.type) {
            FieldType.uuid -> {}
            FieldType.string, FieldType.text, FieldType.enum, FieldType.email, FieldType.password, FieldType.media -> {
                if (value !is String) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }

                when (attribute.type) {
                    FieldType.string -> {
                        if (attribute.length == null) {
                            throw IllegalArgumentException("The length is required for the string type")
                        }

                        if (value.length > attribute.length) {
                            throw IllegalArgumentException("The string length exceeds maximum")
                        }

                        if (!attribute.pattern.isNullOrBlank()) {
                            val regex = attribute.pattern.toRegex()
                            if (!regex.matches(value)) {
                                throw IllegalArgumentException("The string [$value] does not match pattern [${attribute.pattern}]")
                            }
                        }
                    }
                    FieldType.text -> {}
                    FieldType.enum -> {
                        if (attribute.enumSet == null) {
                            throw IllegalArgumentException("enumSet is required for the enum type")
                        }

                        if (value !in attribute.enumSet) {
                            throw IllegalArgumentException(
                                "Enumeration set does not contain value [$value]. Possible values: ${attribute.enumSet.joinToString()}"
                            )
                        }
                    }
                    FieldType.email -> {
                        if (!simpleEmailRegex.matches(value)) {
                            throw IllegalArgumentException("The string [$value] does not match the email pattern")
                        }
                    }
                    FieldType.password -> {
                        if (value.isBlank()) {
                            throw IllegalArgumentException("Password string cannot be blank")
                        }
                    }
                    FieldType.media -> {
                        if (!mediaService.existsById(value)) {
                            throw IllegalArgumentException("Media with ID [$value] does not exist")
                        }
                    }
                    else -> throw IllegalArgumentException("Unsupported attribute type")
                }
            }
            FieldType.sequence -> throw IllegalArgumentException("Sequence cannot be set manually")
            FieldType.int -> {
                if (value !is Int && value !is String) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
                val intValue = if (value is String) value.toInt() else value as Int
                validateAttributeNumberValue(item, attrName, attribute, intValue)
            }
            FieldType.long -> {
                if (value !is Int && value !is Long && value !is String) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
                val longValue = if (value is String) value.toLong() else value as Number
                validateAttributeNumberValue(item, attrName, attribute, longValue)
            }
            FieldType.float, FieldType.double -> {
                if (value !is Float && value !is Double && value !is String) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
                val doubleValue = if (value is String) value.toDouble() else value as Number
                validateAttributeNumberValue(item, attrName, attribute, doubleValue)
            }
            FieldType.decimal -> {
                if (value !is Float && value !is Double && value !is BigDecimal && value !is String) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
                val decimalValue = if (value is String) value.toBigDecimal() else value as Number
                validateAttributeNumberValue(item, attrName, attribute, decimalValue)
            }
            FieldType.date -> {
                if (value !is LocalDate) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
            }
            FieldType.time -> {
                if (value !is OffsetTime) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
            }
            FieldType.datetime -> {
                if (value !is OffsetDateTime) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
            }
            FieldType.timestamp -> {
                if (value !is OffsetDateTime) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
            }
            FieldType.bool -> {
                if (value !is Boolean) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
            }
            FieldType.array -> {
                if (value !is String && value !is List<*>) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
            }
            FieldType.json -> {
                if (value !is String && value !is Map<*, *>) {
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                }
            }
            FieldType.relation -> {
                val targetItem = itemService.getByName(requireNotNull(attribute.target))
                if (attribute.isCollection()) {
                    if (value !is List<*>) {
                        throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                    }

                    val ids = value.filterIsInstance<String>()
                    if (ids.size != value.size) {
                        throw IllegalArgumentException("Wrong type of item [${item.name}] IDs")
                    }

                    if (!itemRecDao.existAllByIds(targetItem, ids.toSet())) {
                        throw IllegalArgumentException("Items [${attribute.target}] with IDs [${ids.joinToString()}] do not exist")
                    }
                } else {
                    if (value !is String) {
                        throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))
                    }

                    val keyAttrName = attribute.referencedBy ?: targetItem.idAttribute
                    if (!itemRecDao.existsByKey(targetItem, keyAttrName, value)) {
                        throw IllegalArgumentException("Item [${attribute.target}] with key [$value] does not exist")
                    }
                }
            }
        }
    }

    private fun validateAttributeNumberValue(item: Item, attrName: String, attribute: Attribute, value: Number) {
        when (value) {
            is Int -> {
                if (attribute.minRange != null && value < attribute.minRange) {
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))
                }

                if (attribute.maxRange != null && value > attribute.maxRange) {
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
                }
            }
            is Long -> {
                if (attribute.minRange != null && value < attribute.minRange) {
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))
                }

                if (attribute.maxRange != null && value > attribute.maxRange) {
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
                }
            }
            is Float -> {
                if (attribute.minRange != null && value < attribute.minRange) {
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))
                }

                if (attribute.maxRange != null && value > attribute.maxRange) {
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
                }
            }
            is Double -> {
                if (attribute.minRange != null && value < attribute.minRange) {
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))
                }

                if (attribute.maxRange != null && value > attribute.maxRange) {
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
                }
            }
            is BigDecimal -> {
                if (attribute.minRange != null && value < attribute.minRange.toBigDecimal()) {
                    throw IllegalArgumentException(VALUE_LESS_THAN_MIN_MSG.format(item.name, attrName, value))
                }

                if (attribute.maxRange != null && value > attribute.maxRange.toBigDecimal()) {
                    throw IllegalArgumentException(VALUE_MORE_THAN_MAX_MSG.format(item.name, attrName, value))
                }
            }
        }
    }

    fun prepareValuesToReturn(item: Item, attributes: Map<String, Any?>): MutableMap<String, Any?> = attributes
        .mapValues { (attrName, value) -> prepareValueToReturn(item, attrName, value) }
        .toMutableMap()
        .withDefault { null }

    fun prepareValueToReturn(item: Item, attrName: String, value: Any?): Any? {
        val attribute = item.spec.getAttribute(attrName)

        return when (attribute.type) {
            FieldType.uuid, FieldType.string, FieldType.text, FieldType.enum, FieldType.email, FieldType.sequence -> value
            FieldType.password -> ItemRec.PASSWORD_PLACEHOLDER
            FieldType.int, FieldType.long, FieldType.float, FieldType.double, FieldType.decimal -> value
            FieldType.date -> value
            FieldType.time -> if (value is OffsetTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value
            FieldType.datetime, FieldType.timestamp -> if (value is OffsetDateTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value
            FieldType.bool -> value
            FieldType.array -> if (value is String) Json.objectMapper.readValue(value, List::class.java) else value
            FieldType.json -> if (value is String) Json.objectMapper.readValue(value, Map::class.java) else value
            FieldType.media, FieldType.relation -> value
        }
    }

    companion object {
        private const val WRONG_VALUE_TYPE_MSG = "Item [%s], attribute [%s], value [%s]: Wrong value type"
        private const val VALUE_LESS_THAN_MIN_MSG = "Item [%s], attribute [%s], value [%s]: The value is less than minRange"
        private const val VALUE_MORE_THAN_MAX_MSG = "Item [%s], attribute [%s], value [%s]: The value is more than maxRange"

        private val excludeAttrNames =
            setOf(
                ItemRec.MAJOR_REV_ATTR_NAME,
                ItemRec.CURRENT_ATTR_NAME,
                ItemRec.LOCALE_ATTR_NAME,
                ItemRec.STATE_ATTR_NAME
            )
        private val simpleEmailRegex = Regex("\\w+@\\w+\\.\\w+")
    }
}
