package ru.scisolutions.scicmscore.graphql

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.Type
import graphql.language.TypeName
import ru.scisolutions.scicmscore.api.model.Property
import java.util.Locale

class TypeResolver {
    fun objectType(name: String, property: Property): Type<*> =
        when (property.type) {
            Property.Type.UUID.value -> {
                val type = if (property.keyed) "ID" else "String"
                typeWithObligation(type, property.required)
            }
            Property.Type.STRING.value, Property.Type.TEXT.value,
            Property.Type.ENUM.value, // TODO: Add enum types
            Property.Type.SEQUENCE.value,
            Property.Type.EMAIL.value, // TODO: Add regexp email scalar type
            Property.Type.PASSWORD.value -> typeWithObligation("String", property.required)
            Property.Type.INT.value -> typeWithObligation("Int", property.required)
            Property.Type.FLOAT.value,
            Property.Type.DECIMAL.value -> typeWithObligation("Float", property.required)
            Property.Type.DATE.value -> typeWithObligation("Date", property.required)
            Property.Type.TIME.value -> typeWithObligation("Time", property.required)
            Property.Type.DATETIME.value -> typeWithObligation("DateTime", property.required)
            Property.Type.TIMESTAMP.value -> typeWithObligation("Int", property.required)
            Property.Type.BOOL.value -> typeWithObligation("Boolean", property.required)
            Property.Type.ARRAY.value,
            Property.Type.JSON.value -> typeWithObligation("String", property.required)
            Property.Type.MEDIA.value -> typeWithObligation("String", property.required)
            Property.Type.RELATION.value -> {
                if (property.target == null)
                    throw IllegalArgumentException("Property [$name]: Target is null")

                val target = property.target
                    .substringBefore("(")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                if (property.relType == Property.RelType.ONE_TO_MANY.value || property.relType == Property.RelType.MANY_TO_MANY.value)
                    ListType(NonNullType(TypeName(target)))
                else
                    typeWithObligation(target, property.required)
            }
            else -> throw IllegalArgumentException("Property [$name]: Invalid type (${property.type})")
        }

    private fun typeWithObligation(type: String, required: Boolean): Type<*> {
        val typeName = TypeName(type)
        return if (required) NonNullType(typeName) else typeName
    }

    fun filterInputType(name: String, property: Property): Type<*> =
        when (property.type) {
            Property.Type.UUID.value -> {
                val type = if (property.keyed) "IDFilterInput" else "StringFilterInput"
                TypeName(type)
            }
            Property.Type.STRING.value, Property.Type.TEXT.value,
            Property.Type.ENUM.value, // TODO: Add enum types
            Property.Type.SEQUENCE.value,
            Property.Type.EMAIL.value, // TODO: Add regexp email scalar type
            Property.Type.PASSWORD.value -> TypeName("StringFilterInput")
            Property.Type.INT.value -> TypeName("IntFilterInput")
            Property.Type.FLOAT.value,
            Property.Type.DECIMAL.value -> TypeName("FloatFilterInput")
            Property.Type.DATE.value -> TypeName("DateFilterInput")
            Property.Type.TIME.value -> TypeName("TimeFilterInput")
            Property.Type.DATETIME.value -> TypeName("DateTimeFilterInput")
            Property.Type.TIMESTAMP.value -> TypeName("IntFilterInput")
            Property.Type.BOOL.value -> TypeName("BooleanFilterInput")
            Property.Type.ARRAY.value,
            Property.Type.JSON.value -> TypeName("StringFilterInput")
            Property.Type.MEDIA.value -> TypeName("StringFilterInput")
            Property.Type.RELATION.value -> {
                if (property.target == null)
                    throw IllegalArgumentException("Property [$name]: Target is null")

                val target = property.target
                    .capitalize()
                    .substringBefore("(")

                TypeName("${target}FiltersInput")
            }
            else -> throw IllegalArgumentException("Property [$name]: Invalid type (${property.type})")
        }

    fun inputType(name: String, property: Property): Type<*> =
        when (property.type) {
            Property.Type.UUID.value -> TypeName("String")
            Property.Type.STRING.value, Property.Type.TEXT.value,
            Property.Type.ENUM.value, // TODO: Add enum types
            Property.Type.SEQUENCE.value,
            Property.Type.EMAIL.value, // TODO: Add regexp email scalar type
            Property.Type.PASSWORD.value -> TypeName("String")
            Property.Type.INT.value -> TypeName("Int")
            Property.Type.FLOAT.value,
            Property.Type.DECIMAL.value -> TypeName("Float")
            Property.Type.DATE.value -> TypeName("Date")
            Property.Type.TIME.value -> TypeName("Time")
            Property.Type.DATETIME.value -> TypeName("DateTime")
            Property.Type.TIMESTAMP.value -> TypeName("Int")
            Property.Type.BOOL.value -> TypeName("Boolean")
            Property.Type.ARRAY.value,
            Property.Type.JSON.value -> TypeName("String")
            Property.Type.MEDIA.value -> TypeName("String")
            Property.Type.RELATION.value -> {
                if (property.target == null)
                    throw IllegalArgumentException("Property [$name]: Target is null")

                if (property.relType == Property.RelType.ONE_TO_MANY.value || property.relType == Property.RelType.MANY_TO_MANY.value)
                    ListType(TypeName("ID"))
                else
                    TypeName("ID")
            }
            else -> throw IllegalArgumentException("Property [$name]: Invalid type (${property.type})")
        }
}