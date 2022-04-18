package ru.scisolutions.scicmscore.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.language.Description
import graphql.language.FieldDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.ObjectTypeExtensionDefinition
import graphql.language.TypeName
import graphql.schema.idl.TypeDefinitionRegistry
import ru.scisolutions.scicmscore.api.model.Property
import ru.scisolutions.scicmscore.api.model.Property.Type
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import java.util.Locale
import graphql.language.Type as GraphQLType

@DgsComponent
class DynamicTypeDefinitions(private val itemService: ItemService) {
    @DgsTypeDefinitionRegistry
    fun registry(): TypeDefinitionRegistry {
        val typeDefinitionRegistry = TypeDefinitionRegistry()
        val queryBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Query")
        val mutationBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Mutation")

        for ((_, item) in itemService.items) {
            typeDefinitionRegistry.add(getTypeDefinition(item))
            typeDefinitionRegistry.add(getResponseTypeDefinition(item))
            typeDefinitionRegistry.add(getResponseCollectionTypeDefinition(item))

            queryBuilder.fieldDefinition(getResponseQueryDefinition(item))
            queryBuilder.fieldDefinition(getResponseCollectionQueryDefinition(item))
            // mutationBuilder.fieldDefinition(getMutationDefinition(item))
        }

        typeDefinitionRegistry.add(queryBuilder.build())
        typeDefinitionRegistry.add(mutationBuilder.build())

        return typeDefinitionRegistry
    }

    private fun getTypeDefinition(item: Item): ObjectTypeDefinition {
        val typeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name(item.name.capitalize())
            .description(Description(item.description, null, true))

        for ((name, property) in item.spec.properties) {
            val type = graphQLType(name, property)
            val fieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
                .name(name)
                .type(type)
                .description(Description(property.description, null, false))

            typeBuilder.fieldDefinition(fieldDefinitionBuilder.build())
        }

        return typeBuilder.build()
    }

    private fun graphQLType(name: String, property: Property): GraphQLType<*> =
        when (property.type) {
            Type.UUID.value -> {
                val type = if (property.keyed) "ID" else "String"
                graphQLTypeWithObligation(type, property.required)
            }
            Type.STRING.value, Type.TEXT.value,
            Type.ENUM.value, // TODO: Add enum types
            Type.SEQUENCE.value,
            Type.EMAIL.value, // TODO: Add regexp email scalar type
            Type.PASSWORD.value -> graphQLTypeWithObligation("String", property.required)
            Type.INT.value -> graphQLTypeWithObligation("Int", property.required)
            Type.FLOAT.value,
            Type.DECIMAL.value -> graphQLTypeWithObligation("Float", property.required)
            Type.DATE.value -> graphQLTypeWithObligation("Date", property.required)
            Type.TIME.value -> graphQLTypeWithObligation("Time", property.required)
            Type.DATETIME.value -> graphQLTypeWithObligation("DateTime", property.required)
            Type.TIMESTAMP.value -> graphQLTypeWithObligation("Int", property.required)
            Type.BOOL.value -> graphQLTypeWithObligation("Boolean", property.required)
            Type.ARRAY.value,
            Type.JSON.value -> graphQLTypeWithObligation("String", property.required)
            Type.MEDIA.value -> graphQLTypeWithObligation("UUID", property.required)
            Type.RELATION.value -> {
                if (property.target == null)
                    throw IllegalArgumentException("Property [$name]: Target is null")

                val target = property.target
                    .substringBefore("(")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                if (property.relType == Property.RelType.ONE_TO_MANY.value || property.relType == Property.RelType.MANY_TO_MANY.value)
                    ListType(NonNullType(TypeName(target)))
                else
                    graphQLTypeWithObligation(target, property.required)
            }
            else -> throw IllegalArgumentException("Property [$name]: Invalid type (${property.type})")
        }

    private fun graphQLTypeWithObligation(type: String, required: Boolean): GraphQLType<*> {
        val typeName = TypeName(type)
        return if (required) NonNullType(typeName) else typeName
    }

    private fun getResponseTypeDefinition(item: Item): ObjectTypeDefinition {
        val typeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${item.name.capitalize()}Response")

        val dataFieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
            .name("data")
            .type(TypeName(item.name.capitalize()))

        typeBuilder.fieldDefinition(dataFieldDefinitionBuilder.build())

        return typeBuilder.build()
    }

    private fun getResponseCollectionTypeDefinition(item: Item): ObjectTypeDefinition {
        val typeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name("${item.name.capitalize()}ResponseCollection")

        val dataFieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
            .name("data")
            .type(
                NonNullType(
                    ListType(
                        NonNullType(
                            TypeName(item.name.capitalize())
                        )
                    )
                )
            )

        typeBuilder.fieldDefinition(dataFieldDefinitionBuilder.build())

        val metaFieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
            .name("meta")
            .type(
                NonNullType(
                    TypeName("ResponseCollectionMeta")
                )
            )

        typeBuilder.fieldDefinition(metaFieldDefinitionBuilder.build())

        return typeBuilder.build()
    }

    private fun getResponseQueryDefinition(item: Item): FieldDefinition {
        return FieldDefinition.newFieldDefinition()
            .name(item.name)
            .type(TypeName("${item.name.capitalize()}Response"))
            // .inputValueDefinition(actionInputValueDefinition())
            // .inputValueDefinition(matchesInputValueDefinition())
            // .inputValueDefinition(whereInputValueDefinition())
            // .inputValueDefinition(sortInputValueDefinition())
            // .inputValueDefinition(limitInputValueDefinition())
            // .inputValueDefinition(offsetInputValueDefinition())
            .build()
    }

    private fun getResponseCollectionQueryDefinition(item: Item): FieldDefinition {
        return FieldDefinition.newFieldDefinition()
            .name(item.pluralName)
            .type(TypeName("${item.name.capitalize()}ResponseCollection"))
            // .inputValueDefinition(actionInputValueDefinition())
            // .inputValueDefinition(matchesInputValueDefinition())
            // .inputValueDefinition(whereInputValueDefinition())
            // .inputValueDefinition(sortInputValueDefinition())
            // .inputValueDefinition(limitInputValueDefinition())
            // .inputValueDefinition(offsetInputValueDefinition())
            .build()
    }
}