package ru.scisolutions.scicmscore.graphql

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.Type
import graphql.language.TypeName
import ru.scisolutions.scicmscore.domain.model.Attribute
import java.util.Locale

class TypeResolver {
    fun objectType(name: String, attribute: Attribute): Type<*> =
        when (attribute.type) {
            Attribute.Type.UUID.value -> {
                val type = if (attribute.keyed) "ID" else "String"
                typeWithObligation(type, attribute.required)
            }
            Attribute.Type.STRING.value, Attribute.Type.TEXT.value,
            Attribute.Type.ENUM.value, // TODO: Add enum types
            Attribute.Type.SEQUENCE.value,
            Attribute.Type.EMAIL.value, // TODO: Add regexp email scalar type
            Attribute.Type.PASSWORD.value -> typeWithObligation("String", attribute.required)
            Attribute.Type.INT.value -> typeWithObligation("Int", attribute.required)
            Attribute.Type.FLOAT.value,
            Attribute.Type.DECIMAL.value -> typeWithObligation("Float", attribute.required)
            Attribute.Type.DATE.value -> typeWithObligation("Date", attribute.required)
            Attribute.Type.TIME.value -> typeWithObligation("Time", attribute.required)
            Attribute.Type.DATETIME.value -> typeWithObligation("DateTime", attribute.required)
            Attribute.Type.TIMESTAMP.value -> typeWithObligation("Int", attribute.required)
            Attribute.Type.BOOL.value -> typeWithObligation("Boolean", attribute.required)
            Attribute.Type.ARRAY.value,
            Attribute.Type.JSON.value -> typeWithObligation("String", attribute.required)
            Attribute.Type.MEDIA.value -> typeWithObligation("String", attribute.required)
            Attribute.Type.RELATION.value -> {
                if (attribute.target == null)
                    throw IllegalArgumentException("Attribute [$name]: Target is null")

                val target = attribute.target
                    .substringBefore("(")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                if (attribute.relType == Attribute.RelType.ONE_TO_MANY.value || attribute.relType == Attribute.RelType.MANY_TO_MANY.value)
                    ListType(NonNullType(TypeName(target)))
                else
                    typeWithObligation(target, attribute.required)
            }
            else -> throw IllegalArgumentException("Attribute [$name]: Invalid type (${attribute.type})")
        }

    private fun typeWithObligation(type: String, required: Boolean): Type<*> {
        val typeName = TypeName(type)
        return if (required) NonNullType(typeName) else typeName
    }

    fun filterInputType(name: String, attribute: Attribute): Type<*> =
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
                    throw IllegalArgumentException("Attribute [$name]: Target is null")

                val target = attribute.target
                    .capitalize()
                    .substringBefore("(")

                TypeName("${target}FiltersInput")
            }
            else -> throw IllegalArgumentException("Attribute [$name]: Invalid type (${attribute.type})")
        }

    fun inputType(name: String, attribute: Attribute): Type<*> =
        when (attribute.type) {
            Attribute.Type.UUID.value -> TypeName("String")
            Attribute.Type.STRING.value, Attribute.Type.TEXT.value,
            Attribute.Type.ENUM.value, // TODO: Add enum types
            Attribute.Type.SEQUENCE.value,
            Attribute.Type.EMAIL.value, // TODO: Add regexp email scalar type
            Attribute.Type.PASSWORD.value -> TypeName("String")
            Attribute.Type.INT.value -> TypeName("Int")
            Attribute.Type.FLOAT.value,
            Attribute.Type.DECIMAL.value -> TypeName("Float")
            Attribute.Type.DATE.value -> TypeName("Date")
            Attribute.Type.TIME.value -> TypeName("Time")
            Attribute.Type.DATETIME.value -> TypeName("DateTime")
            Attribute.Type.TIMESTAMP.value -> TypeName("Int")
            Attribute.Type.BOOL.value -> TypeName("Boolean")
            Attribute.Type.ARRAY.value,
            Attribute.Type.JSON.value -> TypeName("String")
            Attribute.Type.MEDIA.value -> TypeName("String")
            Attribute.Type.RELATION.value -> {
                if (attribute.target == null)
                    throw IllegalArgumentException("Attribute [$name]: Target is null")

                if (attribute.relType == Attribute.RelType.ONE_TO_MANY.value || attribute.relType == Attribute.RelType.MANY_TO_MANY.value)
                    ListType(TypeName("ID"))
                else
                    TypeName("ID")
            }
            else -> throw IllegalArgumentException("Attribute [$name]: Invalid type (${attribute.type})")
        }
}