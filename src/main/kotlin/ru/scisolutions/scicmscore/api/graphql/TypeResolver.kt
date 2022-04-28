package ru.scisolutions.scicmscore.api.graphql

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.engine.schema.model.Attribute
import graphql.language.Type as GraphQLType
import ru.scisolutions.scicmscore.engine.schema.model.Attribute.RelType as AttributeRelType
import ru.scisolutions.scicmscore.engine.schema.model.Attribute.Type as AttributeType

private fun TypeName.nonNull(required: Boolean): GraphQLType<*> = if (required) NonNullType(this) else this

class TypeResolver {
    fun objectType(attrName: String, attribute: Attribute): GraphQLType<*> =
        when (attribute.type) {
            AttributeType.UUID.value -> {
                val typeName = if (attribute.keyed) TypeNames.ID else TypeNames.STRING
                typeName.nonNull(attribute.required)
            }
            AttributeType.STRING.value, AttributeType.TEXT.value,
            AttributeType.ENUM.value,
            AttributeType.SEQUENCE.value,
            AttributeType.EMAIL.value, // TODO: Add regexp email scalar type
            AttributeType.PASSWORD.value -> TypeNames.STRING.nonNull(attribute.required)
            AttributeType.INT.value -> TypeNames.INT.nonNull(attribute.required)
            AttributeType.FLOAT.value,
            AttributeType.DECIMAL.value -> TypeNames.FLOAT.nonNull(attribute.required)
            AttributeType.DATE.value -> TypeNames.DATE.nonNull(attribute.required)
            AttributeType.TIME.value -> TypeNames.TIME.nonNull(attribute.required)
            AttributeType.DATETIME.value,
            AttributeType.TIMESTAMP.value -> TypeNames.DATETIME.nonNull(attribute.required)
            AttributeType.BOOL.value -> TypeNames.BOOLEAN.nonNull(attribute.required)
            AttributeType.ARRAY.value,
            AttributeType.JSON.value,
            AttributeType.MEDIA.value -> TypeNames.STRING.nonNull(attribute.required)
            AttributeType.RELATION.value -> {
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }

                val capitalizedTargetItemName = attribute.target.substringBefore("(").capitalize()
                if (attribute.relType == Attribute.RelType.ONE_TO_MANY.value || attribute.relType == Attribute.RelType.MANY_TO_MANY.value)
                    TypeName("${capitalizedTargetItemName}RelationResponseCollection")
                else
                    TypeName("${capitalizedTargetItemName}RelationResponse").nonNull(attribute.required)
            }
            else -> throw IllegalArgumentException("Attribute [$attrName]: Invalid type (${attribute.type})")
        }

    fun filterInputType(attrName: String, attribute: Attribute): GraphQLType<*> =
        when (attribute.type) {
            AttributeType.UUID.value -> if (attribute.keyed) TypeNames.ID_FILTER_INPUT else TypeNames.STRING_FILTER_INPUT
            AttributeType.STRING.value, AttributeType.TEXT.value,
            AttributeType.ENUM.value,
            AttributeType.SEQUENCE.value,
            AttributeType.EMAIL.value, // TODO: Add regexp email scalar type
            AttributeType.PASSWORD.value -> TypeNames.STRING_FILTER_INPUT
            AttributeType.INT.value -> TypeNames.INT_FILTER_INPUT
            AttributeType.FLOAT.value,
            AttributeType.DECIMAL.value -> TypeNames.FLOAT_FILTER_INPUT
            AttributeType.DATE.value -> TypeNames.DATE_FILTER_INPUT
            AttributeType.TIME.value -> TypeNames.TIME_FILTER_INPUT
            AttributeType.DATETIME.value -> TypeNames.DATETIME_FILTER_INPUT
            AttributeType.TIMESTAMP.value -> TypeNames.DATETIME_FILTER_INPUT
            AttributeType.BOOL.value -> TypeNames.BOOLEAN_FILTER_INPUT
            AttributeType.ARRAY.value,
            AttributeType.JSON.value,
            AttributeType.MEDIA.value -> TypeNames.STRING_FILTER_INPUT
            AttributeType.RELATION.value -> {
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }

                val capitalizedTargetItemName = attribute.target.substringBefore("(").capitalize()
                TypeName("${capitalizedTargetItemName}FiltersInput")
            }
            else -> throw IllegalArgumentException("Attribute [$attrName]: Invalid type (${attribute.type})")
        }

    fun inputType(attrName: String, attribute: Attribute): GraphQLType<*> =
        when (attribute.type) {
            AttributeType.UUID.value -> TypeNames.STRING
            AttributeType.STRING.value,
            AttributeType.TEXT.value,
            AttributeType.ENUM.value,
            AttributeType.SEQUENCE.value,
            AttributeType.EMAIL.value, // TODO: Add regexp email scalar type
            AttributeType.PASSWORD.value -> TypeNames.STRING
            AttributeType.INT.value -> TypeNames.INT
            AttributeType.FLOAT.value,
            AttributeType.DECIMAL.value -> TypeNames.FLOAT
            AttributeType.DATE.value -> TypeNames.DATE
            AttributeType.TIME.value -> TypeNames.TIME
            AttributeType.DATETIME.value -> TypeNames.DATETIME
            AttributeType.TIMESTAMP.value -> TypeNames.DATETIME
            AttributeType.BOOL.value -> TypeNames.BOOLEAN
            AttributeType.ARRAY.value,
            AttributeType.JSON.value,
            AttributeType.MEDIA.value -> TypeNames.STRING
            AttributeType.RELATION.value -> {
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }

                if (attribute.relType == AttributeRelType.ONE_TO_MANY.value || attribute.relType == AttributeRelType.MANY_TO_MANY.value)
                    ListType(TypeNames.ID)
                else
                    TypeNames.ID
            }
            else -> throw IllegalArgumentException("Attribute [$attrName]: Invalid type (${attribute.type})")
        }
}