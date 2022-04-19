package ru.scisolutions.scicmscore.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.language.Description
import graphql.language.FieldDefinition
import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.ObjectTypeExtensionDefinition
import graphql.language.TypeName
import graphql.schema.idl.TypeDefinitionRegistry
import ru.scisolutions.scicmscore.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@DgsComponent
class DynamicTypeDefinitions(private val itemService: ItemService) {
    @DgsTypeDefinitionRegistry
    fun registry(): TypeDefinitionRegistry {
        val typeDefinitionRegistry = TypeDefinitionRegistry()

        // Fill Query
        val queryBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Query")
        for ((name, item) in itemService.items) {
            if (name in excludedQueryItemNames)
                continue

            typeDefinitionRegistry.add(getTypeDefinition(item))
            typeDefinitionRegistry.add(getResponseTypeDefinition(item))
            typeDefinitionRegistry.add(getResponseCollectionTypeDefinition(item))
            typeDefinitionRegistry.add(getFiltersInputTypeDefinition(item))

            queryBuilder.fieldDefinition(getResponseQueryDefinition(item))
            queryBuilder.fieldDefinition(getResponseCollectionQueryDefinition(item))
        }
        typeDefinitionRegistry.add(queryBuilder.build())

        // Fill Mutation
        val mutationBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Mutation")
        for ((name, item) in itemService.items) {
            if (name in excludedMutationItemNames)
                continue

            // mutationBuilder.fieldDefinition(getMutationDefinition(item))
        }
        typeDefinitionRegistry.add(mutationBuilder.build())

        return typeDefinitionRegistry
    }

    private fun getTypeDefinition(item: Item): ObjectTypeDefinition {
        val typeBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name(item.name.capitalize())
            .description(Description(item.description, null, true))

        for ((name, property) in item.spec.properties) {
            val type = typeResolver.objectType(name, property)
            val fieldDefinitionBuilder = FieldDefinition.newFieldDefinition()
                .name(name)
                .type(type)
                .description(Description(property.description, null, false))

            typeBuilder.fieldDefinition(fieldDefinitionBuilder.build())
        }

        return typeBuilder.build()
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
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("id")
                    .type(TypeName("ID"))
                    .build()
            )
            .build()
    }

    private fun getResponseCollectionQueryDefinition(item: Item): FieldDefinition {
        val name = item.name.capitalize()
        return FieldDefinition.newFieldDefinition()
            .name(item.pluralName)
            .type(TypeName("${name}ResponseCollection"))
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("filters")
                    .type(TypeName("${name}FiltersInput"))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("pagination")
                    .type(TypeName("PaginationInput"))
                    .build()
            )
            .inputValueDefinition(
                InputValueDefinition.newInputValueDefinition()
                    .name("sort")
                    .type(ListType(TypeName("String")))
                    .build()
            )
            .build()
    }

    private fun getFiltersInputTypeDefinition(item: Item): InputObjectTypeDefinition {
        val inputName = "${item.name.capitalize()}FiltersInput"
        val inputBuilder = InputObjectTypeDefinition.newInputObjectDefinition()
            .name(inputName)

        for ((name, property) in item.spec.properties) {
            val type = typeResolver.filterInputType(name, property)
            val inputValueDefinitionBuilder = InputValueDefinition.newInputValueDefinition()
                .name(name)
                .type(type)

            inputBuilder.inputValueDefinition(inputValueDefinitionBuilder.build())
        }

        // and
        inputBuilder.inputValueDefinition(
            InputValueDefinition.newInputValueDefinition()
                .name("and")
                .type(ListType(TypeName(inputName)))
                .build()
        )

        // or
        inputBuilder.inputValueDefinition(
            InputValueDefinition.newInputValueDefinition()
                .name("or")
                .type(ListType(TypeName(inputName)))
                .build()
        )

        // not
        inputBuilder.inputValueDefinition(
            InputValueDefinition.newInputValueDefinition()
                .name("not")
                .type(TypeName(inputName))
                .build()
        )

        return inputBuilder.build()
    }

    companion object {
        private const val EXAMPLE = "example"
        private val excludedQueryItemNames = setOf(EXAMPLE)
        private val excludedMutationItemNames = setOf(EXAMPLE) // excludedMutationItemNames should contain names from excludedQueryItemNames
        private val typeResolver = TypeResolver()
    }
}