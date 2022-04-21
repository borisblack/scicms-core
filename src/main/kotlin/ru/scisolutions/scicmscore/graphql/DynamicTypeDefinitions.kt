package ru.scisolutions.scicmscore.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.language.ObjectTypeExtensionDefinition
import graphql.schema.idl.TypeDefinitionRegistry
import ru.scisolutions.scicmscore.service.ItemService

@DgsComponent
class DynamicTypeDefinitions(private val itemService: ItemService) {
    @DgsTypeDefinitionRegistry
    fun registry(): TypeDefinitionRegistry {
        val typeDefinitionRegistry = TypeDefinitionRegistry()

        // Fill Query
        val queryBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Query")
        itemService.items.asSequence()
            .filter { (name, _) -> name !in excludedQueryItemNames }
            .forEach { (_, item) ->
                typeDefinitionRegistry.add(itemTypeDefinitions.getObjectType(item))
                typeDefinitionRegistry.add(itemTypeDefinitions.responseObjectType(item))
                typeDefinitionRegistry.add(itemTypeDefinitions.responseCollectionObjectType(item))
                typeDefinitionRegistry.add(itemTypeDefinitions.filtersInputObjectType(item)) // for filtering
                typeDefinitionRegistry.add(itemTypeDefinitions.inputObjectType(item)) // for mutations

                queryBuilder.fieldDefinition(itemTypeDefinitions.responseQueryField(item))
                queryBuilder.fieldDefinition(itemTypeDefinitions.responseCollectionQueryField(item))
            }

        typeDefinitionRegistry.add(queryBuilder.build())

        // Fill Mutation
        val mutationBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Mutation")
        itemService.items.asSequence()
            .filter { (name, _) -> name !in excludedMutationItemNames }
            .forEach { (_, item) ->
                mutationBuilder.fieldDefinition(itemTypeDefinitions.createMutationField(item))

                if (item.versioned)
                    mutationBuilder.fieldDefinition(itemTypeDefinitions.createVersionMutationField(item))
                else
                    mutationBuilder.fieldDefinition(itemTypeDefinitions.updateMutationField(item))

                if (item.localized)
                    mutationBuilder.fieldDefinition(itemTypeDefinitions.createLocalizationMutationField(item))

                mutationBuilder.fieldDefinition(itemTypeDefinitions.deleteMutationField(item))

                if (item.versioned)
                    mutationBuilder.fieldDefinition(itemTypeDefinitions.purgeMutationField(item))

                mutationBuilder.fieldDefinition(itemTypeDefinitions.lockMutationField(item))
                mutationBuilder.fieldDefinition(itemTypeDefinitions.unlockMutationField(item))
                mutationBuilder.fieldDefinition(itemTypeDefinitions.promoteMutationField(item))

                // Add custom mutations
                if (!item.implementation.isNullOrBlank()) {
                    val customMutations = itemTypeDefinitions.listCustomMutationFields(item)
                    customMutations.forEach { mutationBuilder.fieldDefinition(it) }
                }
            }

        typeDefinitionRegistry.add(mutationBuilder.build())

        return typeDefinitionRegistry
    }

    companion object {
        private const val EXAMPLE = "example"
        private const val ITEM = "item"
        private val excludedQueryItemNames = setOf(EXAMPLE)
        private val excludedMutationItemNames = excludedQueryItemNames.plus(setOf(ITEM)) // excludedMutationItemNames should contain names from excludedQueryItemNames
        private val itemTypeDefinitions = ItemTypeDefinitions()
    }
}