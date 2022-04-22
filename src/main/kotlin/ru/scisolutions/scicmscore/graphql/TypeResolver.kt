package ru.scisolutions.scicmscore.graphql

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.Type
import graphql.language.TypeName
import ru.scisolutions.scicmscore.domain.model.Attribute
import java.util.Locale

class TypeResolver {
    fun objectType(attrName: String, attribute: Attribute): Type<*> =
        when (attribute.type) {
            Attribute.Type.UUID.value -> {
                val type = if (attribute.keyed) TypeNames.ID else TypeNames.STRING
                wrapTypeName(type, attribute.required)
            }
            Attribute.Type.STRING.value, Attribute.Type.TEXT.value,
            Attribute.Type.ENUM.value, // TODO: Add enum types
            Attribute.Type.SEQUENCE.value,
            Attribute.Type.EMAIL.value, // TODO: Add regexp email scalar type
            Attribute.Type.PASSWORD.value -> wrapTypeName(TypeNames.STRING, attribute.required)
            Attribute.Type.INT.value -> wrapTypeName(TypeNames.INT, attribute.required)
            Attribute.Type.FLOAT.value,
            Attribute.Type.DECIMAL.value -> wrapTypeName(TypeNames.FLOAT, attribute.required)
            Attribute.Type.DATE.value -> wrapTypeName(TypeNames.DATE, attribute.required)
            Attribute.Type.TIME.value -> wrapTypeName(TypeNames.TIME, attribute.required)
            Attribute.Type.DATETIME.value -> wrapTypeName(TypeNames.DATETIME, attribute.required)
            Attribute.Type.TIMESTAMP.value -> wrapTypeName(TypeNames.INT, attribute.required)
            Attribute.Type.BOOL.value -> wrapTypeName(TypeNames.BOOLEAN, attribute.required)
            Attribute.Type.ARRAY.value,
            Attribute.Type.JSON.value -> wrapTypeName(TypeNames.STRING, attribute.required)
            Attribute.Type.MEDIA.value -> wrapTypeName(TypeNames.STRING, attribute.required)
            Attribute.Type.RELATION.value -> {
                val capitalizedTargetItemName = attribute.target?.substringBefore("(")?.capitalize()
                    ?: throw IllegalArgumentException("Attribute [$attrName]: Target is null")

                if (attribute.relType == Attribute.RelType.ONE_TO_MANY.value || attribute.relType == Attribute.RelType.MANY_TO_MANY.value)
                    TypeName("${capitalizedTargetItemName}RelationResponseCollection")
                else
                    wrapTypeName(TypeName("${capitalizedTargetItemName}Response"), attribute.required)
            }
            else -> throw IllegalArgumentException("Attribute [$attrName]: Invalid type (${attribute.type})")
        }

    private fun wrapTypeName(typeName: TypeName, required: Boolean): Type<*> =
        if (required) NonNullType(typeName) else typeName

    fun filterInputType(attrName: String, attribute: Attribute): Type<*> =
        when (attribute.type) {
            Attribute.Type.UUID.value -> {
                val type = if (attribute.keyed) "IDFilterInput" else "StringFilterInput"
                TypeName(type)
            }
            Attribute.Type.STRING.value, Attribute.Type.TEXT.value,
            Attribute.Type.ENUM.value, // TODO: Add enum types
            Attribute.Type.SEQUENCE.value,
            Attribute.Type.EMAIL.value, // TODO: Add regexp email scalar type
            Attribute.Type.PASSWORD.value -> TypeName("StringFilterInput")
            Attribute.Type.INT.value -> TypeName("IntFilterInput")
            Attribute.Type.FLOAT.value,
            Attribute.Type.DECIMAL.value -> TypeName("FloatFilterInput")
            Attribute.Type.DATE.value -> TypeName("DateFilterInput")
            Attribute.Type.TIME.value -> TypeName("TimeFilterInput")
            Attribute.Type.DATETIME.value -> TypeName("DateTimeFilterInput")
            Attribute.Type.TIMESTAMP.value -> TypeName("IntFilterInput")
            Attribute.Type.BOOL.value -> TypeName("BooleanFilterInput")
            Attribute.Type.ARRAY.value,
            Attribute.Type.JSON.value -> TypeName("StringFilterInput")
            Attribute.Type.MEDIA.value -> TypeName("StringFilterInput")
            Attribute.Type.RELATION.value -> {
                if (attribute.target == null)
                    throw IllegalArgumentException("Attribute [$attrName]: Target is null")

                val target = attribute.target
                    .capitalize()
                    .substringBefore("(")

                TypeName("${target}FiltersInput")
            }
            else -> throw IllegalArgumentException("Attribute [$attrName]: Invalid type (${attribute.type})")
        }

    fun inputType(attrName: String, attribute: Attribute): Type<*> =
        when (attribute.type) {
            Attribute.Type.UUID.value -> TypeNames.STRING
            Attribute.Type.STRING.value, Attribute.Type.TEXT.value,
            Attribute.Type.ENUM.value, // TODO: Add enum types
            Attribute.Type.SEQUENCE.value,
            Attribute.Type.EMAIL.value, // TODO: Add regexp email scalar type
            Attribute.Type.PASSWORD.value -> TypeNames.STRING
            Attribute.Type.INT.value -> TypeNames.INT
            Attribute.Type.FLOAT.value,
            Attribute.Type.DECIMAL.value -> TypeNames.FLOAT
            Attribute.Type.DATE.value -> TypeNames.DATE
            Attribute.Type.TIME.value -> TypeNames.TIME
            Attribute.Type.DATETIME.value -> TypeNames.DATETIME
            Attribute.Type.TIMESTAMP.value -> TypeNames.INT
            Attribute.Type.BOOL.value -> TypeNames.BOOLEAN
            Attribute.Type.ARRAY.value,
            Attribute.Type.JSON.value,
            Attribute.Type.MEDIA.value -> TypeNames.STRING
            Attribute.Type.RELATION.value -> {
                if (attribute.target == null)
                    throw IllegalArgumentException("Attribute [$attrName]: Target is null")

                if (attribute.relType == Attribute.RelType.ONE_TO_MANY.value || attribute.relType == Attribute.RelType.MANY_TO_MANY.value)
                    ListType(TypeName("ID"))
                else
                    TypeName("ID")
            }
            else -> throw IllegalArgumentException("Attribute [$attrName]: Invalid type (${attribute.type})")
        }
}