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
        for ((name, item) in itemService.items) {
            if (name in excludedQueryItemNames)
                continue

            typeDefinitionRegistry.add(itemTypeDefinitions.getTypeDefinition(item))
            typeDefinitionRegistry.add(itemTypeDefinitions.getResponseTypeDefinition(item))
            typeDefinitionRegistry.add(itemTypeDefinitions.getResponseCollectionTypeDefinition(item))
            typeDefinitionRegistry.add(itemTypeDefinitions.getFiltersInputTypeDefinition(item))
            typeDefinitionRegistry.add(itemTypeDefinitions.getInputTypeDefinition(item))

            queryBuilder.fieldDefinition(itemTypeDefinitions.getResponseQueryDefinition(item))
            queryBuilder.fieldDefinition(itemTypeDefinitions.getResponseCollectionQueryDefinition(item))
        }
        typeDefinitionRegistry.add(queryBuilder.build())

        // Fill Mutation
        val mutationBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Mutation")
        for ((name, item) in itemService.items) {
            if (name in excludedMutationItemNames)
                continue

            mutationBuilder.fieldDefinition(itemTypeDefinitions.getCreateMutationDefinition(item))
            mutationBuilder.fieldDefinition(itemTypeDefinitions.getUpdateMutationDefinition(item))
            mutationBuilder.fieldDefinition(itemTypeDefinitions.getDeleteMutationDefinition(item))
            mutationBuilder.fieldDefinition(itemTypeDefinitions.getPurgeMutationDefinition(item))
            mutationBuilder.fieldDefinition(itemTypeDefinitions.getLockMutationDefinition(item))
            mutationBuilder.fieldDefinition(itemTypeDefinitions.getUnlockMutationDefinition(item))
            mutationBuilder.fieldDefinition(itemTypeDefinitions.getPromoteMutationDefinition(item))
        }
        typeDefinitionRegistry.add(mutationBuilder.build())

        return typeDefinitionRegistry
    }

    companion object {
        private const val EXAMPLE = "example"
        private val excludedQueryItemNames = setOf(EXAMPLE)
        private val excludedMutationItemNames = setOf(EXAMPLE) // excludedMutationItemNames should contain names from excludedQueryItemNames
        private val itemTypeDefinitions = ItemTypeDefinitions()
    }
}