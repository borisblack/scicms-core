package ru.scisolutions.scicmscore.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.language.ObjectTypeExtensionDefinition
import graphql.schema.idl.TypeDefinitionRegistry
import ru.scisolutions.scicmscore.graphql.field.builder.ItemCreateLocalizationMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemCreateMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemCreateVersionMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemCustomMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemDeleteMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemLockMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemPromoteMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemPurgeMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemResponseCollectionQueryFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemResponseQueryFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemUnlockMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.field.builder.ItemUpdateMutationFieldBuilder
import ru.scisolutions.scicmscore.graphql.type.builder.ItemFiltersInputObjectTypeBuilder
import ru.scisolutions.scicmscore.graphql.type.builder.ItemInputObjectTypeBuilder
import ru.scisolutions.scicmscore.graphql.type.builder.ItemObjectTypeBuilder
import ru.scisolutions.scicmscore.graphql.type.builder.ItemResponseCollectionObjectTypeBuilder
import ru.scisolutions.scicmscore.graphql.type.builder.ItemResponseObjectTypeBuilder
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
                typeDefinitionRegistry.add(ItemObjectTypeBuilder(item).build())
                typeDefinitionRegistry.add(ItemResponseObjectTypeBuilder(item).build())
                typeDefinitionRegistry.add(ItemResponseCollectionObjectTypeBuilder(item).build())
                typeDefinitionRegistry.add(ItemFiltersInputObjectTypeBuilder(item).build()) // for filtering
                typeDefinitionRegistry.add(ItemInputObjectTypeBuilder(item).build()) // for mutations

                queryBuilder.fieldDefinition(ItemResponseQueryFieldBuilder(item).build())
                queryBuilder.fieldDefinition(ItemResponseCollectionQueryFieldBuilder(item).build())
            }

        typeDefinitionRegistry.add(queryBuilder.build())

        // Fill Mutation
        val mutationBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Mutation")
        itemService.items.asSequence()
            .filter { (name, _) -> name !in excludedMutationItemNames }
            .forEach { (_, item) ->
                mutationBuilder.fieldDefinition(ItemCreateMutationFieldBuilder(item).build())

                if (item.versioned)
                    mutationBuilder.fieldDefinition(ItemCreateVersionMutationFieldBuilder(item).build())
                else
                    mutationBuilder.fieldDefinition(ItemUpdateMutationFieldBuilder(item).build())

                if (item.localized)
                    mutationBuilder.fieldDefinition(ItemCreateLocalizationMutationFieldBuilder(item).build())

                mutationBuilder.fieldDefinition(ItemDeleteMutationFieldBuilder(item).build())

                if (item.versioned)
                    mutationBuilder.fieldDefinition(ItemPurgeMutationFieldBuilder(item).build())

                mutationBuilder.fieldDefinition(ItemLockMutationFieldBuilder(item).build())
                mutationBuilder.fieldDefinition(ItemUnlockMutationFieldBuilder(item).build())
                mutationBuilder.fieldDefinition(ItemPromoteMutationFieldBuilder(item).build())

                // Add custom mutations
                if (!item.implementation.isNullOrBlank()) {
                    val customMutations = ItemCustomMutationFieldBuilder(item).buildList()
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
    }
}