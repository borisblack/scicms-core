package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.language.ObjectTypeExtensionDefinition
import graphql.schema.idl.TypeDefinitionRegistry
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.CreateFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.CreateLocalizationFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.CreateVersionFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.CustomMethodFieldListBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.DeleteFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.LockFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.PromoteFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.PurgeFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.UnlockFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.mutation.UpdateFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.query.ResponseCollectionFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.field.builder.query.ResponseFieldBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.CustomMethodResponseObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.ItemObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.RelationResponseCollectionObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.RelationResponseObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.ResponseCollectionObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.ResponseObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.input.FiltersInputObjectTypeBuilder
import ru.scisolutions.scicmscore.api.graphql.type.builder.input.ItemInputObjectTypeBuilder
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.service.ItemService

@DgsComponent
class DynamicTypeDefinitions(
    private val itemService: ItemService,
    private val dataEngine: DataEngine,
    private val itemObjectTypeBuilder: ItemObjectTypeBuilder,
    private val filtersInputObjectTypeBuilder: FiltersInputObjectTypeBuilder,
    private val itemInputObjectTypeBuilder: ItemInputObjectTypeBuilder
) {
    @DgsTypeDefinitionRegistry
    fun registry(): TypeDefinitionRegistry {
        val typeDefinitionRegistry = TypeDefinitionRegistry()
        val items = itemService.findAll()

        // Fill Query
        val queryBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Query")
        items.asSequence()
            .filter { !excludeItemPolicy.excludeFromQuery(it) }
            .forEach {
                typeDefinitionRegistry.add(itemObjectTypeBuilder.fromItem(it))
                typeDefinitionRegistry.add(ResponseObjectTypeBuilder().fromItem(it))
                typeDefinitionRegistry.add(RelationResponseObjectTypeBuilder().fromItem(it))
                typeDefinitionRegistry.add(ResponseCollectionObjectTypeBuilder().fromItem(it))
                typeDefinitionRegistry.add(RelationResponseCollectionObjectTypeBuilder().fromItem(it))
                typeDefinitionRegistry.add(filtersInputObjectTypeBuilder.fromItem(it)) // for filtering
                typeDefinitionRegistry.add(itemInputObjectTypeBuilder.fromItem(it)) // for mutations

                queryBuilder.fieldDefinition(ResponseFieldBuilder().fromItem(it))
                queryBuilder.fieldDefinition(ResponseCollectionFieldBuilder().fromItem(it))
            }

        typeDefinitionRegistry.add(queryBuilder.build())

        // Fill Mutation
        val mutationBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Mutation")
        items.asSequence()
            .filter { !excludeItemPolicy.excludeFromMutation(it) }
            .forEach {
                if (!excludeItemPolicy.excludeFromCreateMutation(it))
                    mutationBuilder.fieldDefinition(CreateFieldBuilder().fromItem(it))

                if (it.versioned)
                    mutationBuilder.fieldDefinition(CreateVersionFieldBuilder().fromItem(it))
                else if (!excludeItemPolicy.excludeFromUpdateMutation(it))
                    mutationBuilder.fieldDefinition(UpdateFieldBuilder().fromItem(it))

                if (it.localized)
                    mutationBuilder.fieldDefinition(CreateLocalizationFieldBuilder().fromItem(it))

                mutationBuilder.fieldDefinition(DeleteFieldBuilder().fromItem(it))

                if (it.versioned)
                    mutationBuilder.fieldDefinition(PurgeFieldBuilder().fromItem(it))

                mutationBuilder.fieldDefinition(LockFieldBuilder().fromItem(it))
                mutationBuilder.fieldDefinition(UnlockFieldBuilder().fromItem(it))
                mutationBuilder.fieldDefinition(PromoteFieldBuilder().fromItem(it))

                // Add custom mutations
                if (it.implementation != null) {
                    typeDefinitionRegistry.add(CustomMethodResponseObjectTypeBuilder().fromItem(it))

                    val customMutations = CustomMethodFieldListBuilder(it.name, dataEngine.getCustomMethods(it.name)).buildList()
                    customMutations.forEach { mutationBuilder.fieldDefinition(it) }
                }
            }

        typeDefinitionRegistry.add(mutationBuilder.build())

        return typeDefinitionRegistry
    }

    companion object {
        private val excludeItemPolicy = ExcludeItemPolicy()
    }
}