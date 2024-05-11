package ru.scisolutions.scicmscore.api.graphql.type

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.TypeName
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.api.graphql.TypeNames
import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.schema.service.RelationValidator
import ru.scisolutions.scicmscore.extension.upperFirst
import graphql.language.Type as GraphQLType

private fun TypeName.nonNull(required: Boolean): GraphQLType<*> = if (required) NonNullType(this) else this

@Component
class AttributeTypes(
    private val itemService: ItemService,
    private val relationValidator: RelationValidator
) {
    fun objectType(item: Item, attrName: String, attribute: Attribute): GraphQLType<*> {
        if (attribute.keyed)
            return TypeNames.ID.nonNull(attribute.required)

        return when (attribute.type) {
            FieldType.uuid -> TypeNames.UUID.nonNull(attribute.required)
            FieldType.string, FieldType.text, FieldType.enum, FieldType.sequence -> TypeNames.STRING.nonNull(attribute.required)
            FieldType.email -> TypeNames.EMAIL.nonNull(attribute.required)
            FieldType.password -> TypeNames.STRING.nonNull(attribute.required)
            FieldType.int, FieldType.long -> TypeNames.INT.nonNull(attribute.required)
            FieldType.float, FieldType.double, FieldType.decimal -> TypeNames.FLOAT.nonNull(attribute.required)
            FieldType.date -> TypeNames.DATE.nonNull(attribute.required)
            FieldType.time -> TypeNames.TIME.nonNull(attribute.required)
            FieldType.datetime, FieldType.timestamp -> TypeNames.DATETIME.nonNull(attribute.required)
            FieldType.bool -> TypeNames.BOOLEAN.nonNull(attribute.required)
            FieldType.array, FieldType.json -> TypeNames.JSON.nonNull(attribute.required)
            FieldType.media -> TypeName("MediaRelationResponse").nonNull(attribute.required)
            FieldType.relation -> {
                relationValidator.validateAttribute(item, attrName, attribute)

                val capitalizedTargetItemName = requireNotNull(attribute.target).upperFirst()
                if (attribute.isCollection())
                    TypeName("${capitalizedTargetItemName}RelationResponseCollection")
                else
                    TypeName("${capitalizedTargetItemName}RelationResponse").nonNull(attribute.required)
            }
        }
    }

    fun filterInputType(item: Item, attrName: String, attribute: Attribute): GraphQLType<*> {
        if (attribute.keyed)
            return TypeNames.ID_FILTER_INPUT

        return when (attribute.type) {
            FieldType.uuid -> TypeNames.UUID_FILTER_INPUT
            FieldType.string, FieldType.text, FieldType.enum, FieldType.sequence, FieldType.email, FieldType.password -> TypeNames.STRING_FILTER_INPUT
            FieldType.int, FieldType.long -> TypeNames.INT_FILTER_INPUT
            FieldType.float, FieldType.double, FieldType.decimal -> TypeNames.FLOAT_FILTER_INPUT
            FieldType.date -> TypeNames.DATE_FILTER_INPUT
            FieldType.time -> TypeNames.TIME_FILTER_INPUT
            FieldType.datetime -> TypeNames.DATETIME_FILTER_INPUT
            FieldType.timestamp -> TypeNames.DATETIME_FILTER_INPUT
            FieldType.bool -> TypeNames.BOOLEAN_FILTER_INPUT
            FieldType.array, FieldType.json -> TypeNames.STRING_FILTER_INPUT

            FieldType.media -> {
                val media = itemService.getMedia()
                if (media.ds == item.ds)
                    TypeName("MediaFiltersInput")
                else
                    TypeNames.ID_FILTER_INPUT
            }

            FieldType.relation -> {
                relationValidator.validateAttribute(item, attrName, attribute)

                val targetItem = itemService.getByName(requireNotNull(attribute.target))
                if (targetItem.ds == item.ds) {
                    val capitalizedTargetItemName = attribute.target.upperFirst()
                    TypeName("${capitalizedTargetItemName}FiltersInput")
                } else {
                    if (attribute.isCollection())
                        throw IllegalArgumentException("Filtering collections from different datasource is not supported.")

                    TypeNames.ID_FILTER_INPUT
                }
            }
        }
    }

    fun inputType(item: Item, attrName: String, attribute: Attribute): GraphQLType<*> {
        if (attribute.keyed)
            return TypeNames.ID

        return when (attribute.type) {
            FieldType.uuid -> TypeNames.UUID
            FieldType.string, FieldType.text -> TypeNames.STRING
            // FieldType.enum -> TypeName("${item.name.upperFirst()}${attrName.upperFirst()}Enum")
            FieldType.enum, FieldType.sequence -> TypeNames.STRING
            FieldType.email -> TypeNames.EMAIL
            FieldType.password -> TypeNames.STRING
            FieldType.int, FieldType.long -> TypeNames.INT
            FieldType.float, FieldType.double, FieldType.decimal -> TypeNames.FLOAT
            FieldType.date -> TypeNames.DATE
            FieldType.time -> TypeNames.TIME
            FieldType.datetime -> TypeNames.DATETIME
            FieldType.timestamp -> TypeNames.DATETIME
            FieldType.bool -> TypeNames.BOOLEAN
            FieldType.array, FieldType.json -> TypeNames.JSON
            FieldType.media -> TypeNames.ID
            FieldType.relation -> {
                relationValidator.validateAttribute(item, attrName, attribute)

                if (attribute.isCollection()) ListType(TypeNames.ID) else TypeNames.ID
            }
        }
    }
}