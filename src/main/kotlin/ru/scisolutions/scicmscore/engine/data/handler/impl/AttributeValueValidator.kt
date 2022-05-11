package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.service.MediaService
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.util.UUID

@Component
class AttributeValueValidator(
    private val itemService: ItemService,
    private val mediaService: MediaService,
    private val itemRecDao: ItemRecDao
) {
    fun validate(item: Item, attrName: String, attribute: Attribute, value: Any) {
        when (attribute.type) {
            Type.uuid, Type.string, Type.text, Type.enum, Type.email, Type.password, Type.media -> {
                if (value !is String)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                when (attribute.type) {
                    Type.uuid -> UUID.fromString(value)
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
                            throw IllegalArgumentException("enumSet does not contain value [$value]")
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
                    else -> throw IllegalArgumentException("Unsupported attribute type")
                }
            }
            Type.sequence -> throw IllegalArgumentException("Sequence cannot be set manually")
            Type.int -> {
                if (value !is Int)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateNumber(item, attrName, attribute, value)
            }
            Type.long -> {
                if (value !is Int && value !is Long)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateNumber(item, attrName, attribute, value as Number)
            }
            Type.float -> {
                if (value !is Float)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateNumber(item, attrName, attribute, value)
            }
            Type.double -> {
                if (value !is Float && value !is Double)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateNumber(item, attrName, attribute, value as Number)
            }
            Type.decimal -> {
                if (value !is Float && value !is Double && value !is BigDecimal)
                    throw IllegalArgumentException(WRONG_VALUE_TYPE_MSG.format(item.name, attrName, value))

                validateNumber(item, attrName, attribute, value as Number)
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
            else -> throw IllegalArgumentException("Unsupported attribute type")
        }
    }

    private fun validateNumber(item: Item, attrName: String, attribute: Attribute, value: Number) {
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

    companion object {
        private const val WRONG_VALUE_TYPE_MSG = "Item [%s], attribute [%s], value [%s]: Wrong value type"
        private const val VALUE_LESS_THAN_MIN_MSG = "Item [%s], attribute [%s], value [%s]: The value is less than minRange"
        private const val VALUE_MORE_THAN_MAX_MSG = "Item [%s], attribute [%s], value [%s]: The value is more than maxRange"
        private const val LOCALE_ATTR_NAME = "locale"

        private val simpleEmailRegex = Regex("\\w+@\\w+\\.\\w+")
    }
}