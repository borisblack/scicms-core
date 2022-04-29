package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.DgsCodeRegistry
import com.netflix.graphql.dgs.DgsComponent
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.CreateDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.CreateLocalizationDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.CreateVersionDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.CustomMethodDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.DeleteDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.LockDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.PromoteDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.PurgeDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.UnlockDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation.UpdateDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.query.RelationResponseCollectionDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.query.RelationResponseDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.query.ResponseCollectionDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.query.ResponseDataFetcher
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.domain.model.Attribute.Type as AttrType

@DgsComponent
class DynamicDataFetcher(
    private val itemService: ItemService,
    private val dataEngine: DataEngine,
    private val responseDataFetcher: ResponseDataFetcher,
    private val relationResponseDataFetcher: RelationResponseDataFetcher,
    private val customMethodDataFetcher: CustomMethodDataFetcher
) {
    @DgsCodeRegistry
    fun registry(codeRegistryBuilder: GraphQLCodeRegistry.Builder, registry: TypeDefinitionRegistry): GraphQLCodeRegistry.Builder {
        // Query
        itemService.items.asSequence()
            .filter { (_, item) -> !excludeItemPolicy.excludeFromQuery(item) }
            .forEach { (_, item) ->
                addAttributeDataFetchers(codeRegistryBuilder, item)

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(QUERY_TYPE, item.name), responseDataFetcher)
                    .dataFetcher(FieldCoordinates.coordinates(QUERY_TYPE, item.pluralName), ResponseCollectionDataFetcher())
            }

        // Mutation
        itemService.items.asSequence()
            .filter { (_, item) -> !excludeItemPolicy.excludeFromMutation(item) }
            .forEach { (itemName, item) ->
                val capitalizedItemName = item.name.capitalize()

                if (!excludeItemPolicy.excludeFromCreateMutation(item))
                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "create${capitalizedItemName}"), CreateDataFetcher())

                if (item.versioned) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "create${capitalizedItemName}Version"), CreateVersionDataFetcher())
                } else if (!excludeItemPolicy.excludeFromUpdateMutation(item)) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "update${capitalizedItemName}"), UpdateDataFetcher())
                }

                if (item.localized) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "create${capitalizedItemName}Localization"), CreateLocalizationDataFetcher())
                }

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "delete${capitalizedItemName}"), DeleteDataFetcher())

                if (item.versioned) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "purge${capitalizedItemName}"), PurgeDataFetcher())
                }

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "lock${capitalizedItemName}"), LockDataFetcher())

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "unlock${capitalizedItemName}"), UnlockDataFetcher())

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "promote${capitalizedItemName}"), PromoteDataFetcher())

                // Custom methods
                if (item.implementation != null) {
                    val customMethodNames = dataEngine.getCustomMethods(itemName)
                    for (methodName in customMethodNames) {
                        codeRegistryBuilder
                            .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "${methodName}${capitalizedItemName}"), customMethodDataFetcher)
                    }
                }
            }

        return codeRegistryBuilder
    }

    private fun addAttributeDataFetchers(codeRegistryBuilder: GraphQLCodeRegistry.Builder, item: Item) {
        val capitalizedItemName = item.name.capitalize()
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> AttrType.valueOf(attribute.type) == AttrType.relation }
            .forEach { (attrName, attribute) ->
                val attrRelType = RelType.nullableValueOf(attribute.relType)
                if (attrRelType == RelType.oneToOne || attrRelType == RelType.manyToOne) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(capitalizedItemName, attrName), relationResponseDataFetcher)
                } else if (attrRelType == RelType.oneToMany || attrRelType == RelType.manyToMany) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(capitalizedItemName, attrName), RelationResponseCollectionDataFetcher())
                }
            }
    }

    companion object {
        private const val QUERY_TYPE = "Query"
        private const val MUTATION_TYPE = "Mutation"

        private val excludeItemPolicy = ExcludeItemPolicy()
    }
}