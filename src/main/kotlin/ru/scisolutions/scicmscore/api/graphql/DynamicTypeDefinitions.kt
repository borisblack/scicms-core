package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.language.ObjectTypeExtensionDefinition
import graphql.schema.idl.TypeDefinitionRegistry
import ru.scisolutions.scicmscore.api.graphql.field.MutationItemFields
import ru.scisolutions.scicmscore.api.graphql.field.QueryItemFields
import ru.scisolutions.scicmscore.api.graphql.type.ItemInputObjectTypes
import ru.scisolutions.scicmscore.api.graphql.type.ItemObjectTypes
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.persistence.service.ItemService

@DgsComponent
class DynamicTypeDefinitions(
    private val itemService: ItemService,
    private val engine: Engine,
    private val itemObjectTypes: ItemObjectTypes,
    private val itemInputObjectTypes: ItemInputObjectTypes,
    private val queryItemFields: QueryItemFields,
    private val mutationItemFields: MutationItemFields
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
                typeDefinitionRegistry.add(itemObjectTypes.item(it))
                typeDefinitionRegistry.add(itemObjectTypes.response(it))
                typeDefinitionRegistry.add(itemObjectTypes.relationResponse(it))
                typeDefinitionRegistry.add(itemObjectTypes.responseCollection(it))
                typeDefinitionRegistry.add(itemObjectTypes.relationResponseCollection(it))
                typeDefinitionRegistry.add(itemInputObjectTypes.filtersInput(it))
                typeDefinitionRegistry.add(itemInputObjectTypes.itemInput(it))

                // itemInputObjectTypes.enumTypes(it).forEach { enumType ->
                //     typeDefinitionRegistry.add(enumType)
                // }

                queryBuilder.fieldDefinition(queryItemFields.item(it))
                queryBuilder.fieldDefinition(queryItemFields.itemCollection(it))
            }

        typeDefinitionRegistry.add(queryBuilder.build())

        // Fill Mutation
        val mutationBuilder = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition().name("Mutation")
        items.asSequence()
            .filter { !it.readOnly }
            .filter { !excludeItemPolicy.excludeFromMutation(it) }
            .forEach {
                if (!excludeItemPolicy.excludeFromCreateMutation(it))
                    mutationBuilder.fieldDefinition(mutationItemFields.create(it))

                if (it.versioned)
                    mutationBuilder.fieldDefinition(mutationItemFields.createVersion(it))
                else if (!excludeItemPolicy.excludeFromUpdateMutation(it))
                    mutationBuilder.fieldDefinition(mutationItemFields.update(it))

                if (it.localized)
                    mutationBuilder.fieldDefinition(mutationItemFields.createLocalization(it))

                mutationBuilder.fieldDefinition(mutationItemFields.delete(it))

                if (it.versioned)
                    mutationBuilder.fieldDefinition(mutationItemFields.purge(it))

                if (!it.notLockable) {
                    typeDefinitionRegistry.add(itemObjectTypes.flaggedResponse(it))
                    mutationBuilder.fieldDefinition(mutationItemFields.lock(it))
                    mutationBuilder.fieldDefinition(mutationItemFields.unlock(it))
                }

                mutationBuilder.fieldDefinition(mutationItemFields.promote(it))

                // Add custom mutations
                if (it.implementation != null) {
                    typeDefinitionRegistry.add(itemObjectTypes.customMethodResponse(it))

                    val customMethods = mutationItemFields.customMethods(it, engine.getCustomMethods(it.name))
                    customMethods.forEach { method -> mutationBuilder.fieldDefinition(method) }
                }
            }

        typeDefinitionRegistry.add(mutationBuilder.build())

        return typeDefinitionRegistry
    }

    companion object {
        private val excludeItemPolicy = ExcludeItemPolicy()
    }
}