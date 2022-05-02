package ru.scisolutions.scicmscore.api.graphql

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.TypeName
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import graphql.language.Type as GraphQLType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type as AttrType

private fun TypeName.nonNull(required: Boolean): GraphQLType<*> = if (required) NonNullType(this) else this

class TypeResolver {
    fun objectType(attrName: String, attribute: Attribute): GraphQLType<*> =
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
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }

                val capitalizedTargetItemName = attribute.extractTarget().capitalize()
                if (attribute.relType == RelType.oneToMany || attribute.relType == RelType.manyToMany)
                    TypeName("${capitalizedTargetItemName}RelationResponseCollection")
                else
                    TypeName("${capitalizedTargetItemName}RelationResponse").nonNull(attribute.required)
            }
            else -> throw IllegalArgumentException("Attribute [$attrName] has unsupported type (${attribute.type})")
        }

    fun filterInputType(attrName: String, attribute: Attribute): GraphQLType<*> =
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
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }

                val capitalizedTargetItemName = attribute.extractTarget().capitalize()
                TypeName("${capitalizedTargetItemName}FiltersInput")
                // TypeNames.ID_FILTER_INPUT
            }
            else -> throw IllegalArgumentException("Attribute [$attrName] has unsupported type (${attribute.type})")
        }

    fun inputType(attrName: String, attribute: Attribute): GraphQLType<*> =
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
                requireNotNull(attribute.relType) { "Attribute [$attrName] has a relation type, but relType is null" }
                requireNotNull(attribute.target) { "Attribute [$attrName] has a relation type, but target is null" }

                if (attribute.relType == RelType.oneToMany || attribute.relType == RelType.manyToMany)
                    ListType(TypeNames.ID)
                else
                    TypeNames.ID
            }
            else -> throw IllegalArgumentException("Attribute [$attrName] has unsupported type (${attribute.type})")
        }
}