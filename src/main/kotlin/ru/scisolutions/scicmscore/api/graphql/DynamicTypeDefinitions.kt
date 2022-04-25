package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.language.ObjectTypeExtensionDefinition
import graphql.schema.idl.TypeDefinitionRegistry
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.CreateFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.CreateLocalizationFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.CreateVersionFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.DeleteFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.ImplementationFieldListBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.LockFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.PromoteFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.PurgeFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.UnlockFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.UpdateFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.query.ResponseCollectionFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.query.ResponseFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.ItemObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.RelationResponseCollectionObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.ResponseCollectionObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.ResponseObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.input.FiltersInputObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.input.ItemInputObjectTypeBuilder
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
                typeDefinitionRegistry.add(ResponseObjectTypeBuilder(item).build())
                typeDefinitionRegistry.add(ResponseCollectionObjectTypeBuilder(item).build())
                typeDefinitionRegistry.add(RelationResponseCollectionObjectTypeBuilder(item).build())
                typeDefinitionRegistry.add(FiltersInputObjectTypeBuilder(item).build()) // for filtering
                typeDefinitionRegistry.add(ItemInputObjectTypeBuilder(item).build()) // for mutations

                queryBuilder.fieldDefinition(ResponseFieldBuilder(item).build())
                queryBuilder.fieldDefinition(ResponseCollectionFieldBuilder(item).build())
            }

        typeDefinitionRegistry.add(queryBuilder.build())

        // Fill Mutation
        val mutationBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Mutation")
        itemService.items.asSequence()
            .filter { (name, _) -> name !in excludedMutationItemNames }
            .forEach { (name, item) ->
                if (name !in excludedCreateItemNames)
                    mutationBuilder.fieldDefinition(CreateFieldBuilder(item).build())

                if (item.versioned)
                    mutationBuilder.fieldDefinition(CreateVersionFieldBuilder(item).build())
                else if (name !in excludedUpdateItemNames)
                    mutationBuilder.fieldDefinition(UpdateFieldBuilder(item).build())

                if (item.localized)
                    mutationBuilder.fieldDefinition(CreateLocalizationFieldBuilder(item).build())

                mutationBuilder.fieldDefinition(DeleteFieldBuilder(item).build())

                if (item.versioned)
                    mutationBuilder.fieldDefinition(PurgeFieldBuilder(item).build())

                mutationBuilder.fieldDefinition(LockFieldBuilder(item).build())
                mutationBuilder.fieldDefinition(UnlockFieldBuilder(item).build())
                mutationBuilder.fieldDefinition(PromoteFieldBuilder(item).build())

                // Add custom mutations
                if (!item.implementation.isNullOrBlank()) {
                    val customMutations = ImplementationFieldListBuilder(item).buildList()
                    customMutations.forEach { mutationBuilder.fieldDefinition(it) }
                }
            }

        typeDefinitionRegistry.add(mutationBuilder.build())

        return typeDefinitionRegistry
    }

    companion object {
        private const val EXAMPLE_ITEM_NAME = "example"
        private const val ITEM_ITEM_NAME = "item"
        private const val MEDIA_ITEM_NAME = "media"
        private val excludedQueryItemNames = setOf(EXAMPLE_ITEM_NAME)
        private val excludedMutationItemNames = excludedQueryItemNames.plus(setOf(ITEM_ITEM_NAME))
        private val excludedCreateItemNames = setOf(MEDIA_ITEM_NAME)
        private val excludedUpdateItemNames = setOf(MEDIA_ITEM_NAME)
    }
}