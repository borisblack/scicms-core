package ru.scisolutions.scicmscore.api.graphql.type

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.service.impl.RelationValidator
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import graphql.language.Type as GraphQLType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type as AttrType

private fun TypeName.nonNull(required: Boolean): GraphQLType<*> = if (required) NonNullType(this) else this

@Component
class AttributeTypes(
    private val itemService: ItemService,
    private val relationValidator: RelationValidator
) {
    fun objectType(item: Item, attrName: String, attribute: Attribute): GraphQLType<*> =
        when (attribute.type) {
            AttrType.uuid -> {
                val typeName = if (attribute.keyed) TypeNames.ID else TypeNames.STRING
                typeName.nonNull(attribute.required)
            }
            AttrType.string, AttrType.text, AttrType.enum, AttrType.sequence,
            AttrType.email, // TODO: Add regexp email scalar type
            AttrType.password -> TypeNames.STRING.nonNull(attribute.required)
            AttrType.int, AttrType.long -> TypeNames.INT.nonNull(attribute.required)
            AttrType.float, AttrType.double, AttrType.decimal -> TypeNames.FLOAT.nonNull(attribute.required)
            AttrType.date -> TypeNames.DATE.nonNull(attribute.required)
            AttrType.time -> TypeNames.TIME.nonNull(attribute.required)
            AttrType.datetime, AttrType.timestamp -> TypeNames.DATETIME.nonNull(attribute.required)
            AttrType.bool -> TypeNames.BOOLEAN.nonNull(attribute.required)
            AttrType.array, AttrType.json -> TypeNames.JSON.nonNull(attribute.required)
            AttrType.media -> TypeNames.STRING.nonNull(attribute.required)
            AttrType.relation -> {
                relationValidator.validateAttribute(item, attrName, attribute)

                val capitalizedTargetItemName = requireNotNull(attribute.target).capitalize()
                if (attribute.isCollection())
                    TypeName("${capitalizedTargetItemName}RelationResponseCollection")
                else
                    TypeName("${capitalizedTargetItemName}RelationResponse").nonNull(attribute.required)
            }
            else -> throw IllegalArgumentException("Attribute [$attrName] has unsupported type (${attribute.type})")
        }

    fun filterInputType(item: Item, attrName: String, attribute: Attribute): GraphQLType<*> =
        when (attribute.type) {
            AttrType.uuid -> if (attribute.keyed) TypeNames.ID_FILTER_INPUT else TypeNames.STRING_FILTER_INPUT
            AttrType.string, AttrType.text, AttrType.enum, AttrType.sequence,
            AttrType.email, // TODO: Add regexp email scalar type
            AttrType.password -> TypeNames.STRING_FILTER_INPUT
            AttrType.int, AttrType.long -> TypeNames.INT_FILTER_INPUT
            AttrType.float, AttrType.double, AttrType.decimal -> TypeNames.FLOAT_FILTER_INPUT
            AttrType.date -> TypeNames.DATE_FILTER_INPUT
            AttrType.time -> TypeNames.TIME_FILTER_INPUT
            AttrType.datetime -> TypeNames.DATETIME_FILTER_INPUT
            AttrType.timestamp -> TypeNames.DATETIME_FILTER_INPUT
            AttrType.bool -> TypeNames.BOOLEAN_FILTER_INPUT
            AttrType.array,
            AttrType.json,
            AttrType.media -> TypeNames.STRING_FILTER_INPUT
            AttrType.relation -> {
                relationValidator.validateAttribute(item, attrName, attribute)

                val targetItem = itemService.getByName(requireNotNull(attribute.target))
                if (targetItem.dataSource == item.dataSource) {
                    val capitalizedTargetItemName = attribute.target.capitalize()
                    TypeName("${capitalizedTargetItemName}FiltersInput")
                } else {
                    if (attribute.isCollection())
                        throw IllegalArgumentException("Filtering collections from different datasource is not supported")

                    TypeNames.ID_FILTER_INPUT
                }
            }
            else -> throw IllegalArgumentException("Attribute [$attrName] has unsupported type (${attribute.type})")
        }

    fun inputType(item: Item, attrName: String, attribute: Attribute): GraphQLType<*> =
        when (attribute.type) {
            AttrType.uuid -> TypeNames.STRING
            AttrType.string, AttrType.text, AttrType.enum, AttrType.sequence,
            AttrType.email, // TODO: Add regexp email scalar type
            AttrType.password -> TypeNames.STRING
            AttrType.int, AttrType.long -> TypeNames.INT
            AttrType.float, AttrType.double, AttrType.decimal -> TypeNames.FLOAT
            AttrType.date -> TypeNames.DATE
            AttrType.time -> TypeNames.TIME
            AttrType.datetime -> TypeNames.DATETIME
            AttrType.timestamp -> TypeNames.DATETIME
            AttrType.bool -> TypeNames.BOOLEAN
            AttrType.array, AttrType.json, AttrType.media -> TypeNames.STRING
            AttrType.relation -> {
                relationValidator.validateAttribute(item, attrName, attribute)

                if (attribute.isCollection()) ListType(TypeNames.ID) else TypeNames.ID
            }
            else -> throw IllegalArgumentException("Attribute [$attrName] has unsupported type (${attribute.type})")
        }
}