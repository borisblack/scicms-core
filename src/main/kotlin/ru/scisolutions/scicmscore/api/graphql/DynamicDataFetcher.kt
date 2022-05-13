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
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService

@DgsComponent
class DynamicDataFetcher(
    private val itemService: ItemService,
    private val dataEngine: DataEngine,
    private val responseDataFetcher: ResponseDataFetcher,
    private val relationResponseDataFetcher: RelationResponseDataFetcher,
    private val responseCollectionDataFetcher: ResponseCollectionDataFetcher,
    private val relationResponseCollectionDataFetcher: RelationResponseCollectionDataFetcher,
    private val createDataFetcher: CreateDataFetcher,
    private val createVersionDataFetcher: CreateVersionDataFetcher,
    private val createLocalizationDataFetcher: CreateLocalizationDataFetcher,
    private val updateDataFetcher: UpdateDataFetcher,
    private val lockDataFetcher: LockDataFetcher,
    private val unlockDataFetcher: UnlockDataFetcher,
    private val customMethodDataFetcher: CustomMethodDataFetcher
) {
    @DgsCodeRegistry
    fun registry(codeRegistryBuilder: GraphQLCodeRegistry.Builder, registry: TypeDefinitionRegistry): GraphQLCodeRegistry.Builder {
        val items = itemService.findAll()
        // Query
        items.asSequence()
            .filter { !excludeItemPolicy.excludeFromQuery(it) }
            .forEach {
                addAttributeDataFetchers(codeRegistryBuilder, it)

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(QUERY_TYPE, it.name), responseDataFetcher)
                    .dataFetcher(FieldCoordinates.coordinates(QUERY_TYPE, it.pluralName), responseCollectionDataFetcher)
            }

        // Mutation
        items.asSequence()
            .filter { !excludeItemPolicy.excludeFromMutation(it) }
            .forEach {
                val capitalizedItemName = it.name.capitalize()

                if (!excludeItemPolicy.excludeFromCreateMutation(it))
                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "create${capitalizedItemName}"), createDataFetcher)

                if (it.versioned) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "create${capitalizedItemName}Version"), createVersionDataFetcher)
                } else if (!excludeItemPolicy.excludeFromUpdateMutation(it)) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "update${capitalizedItemName}"), updateDataFetcher)
                }

                if (it.localized) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "create${capitalizedItemName}Localization"), createLocalizationDataFetcher)
                }

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "delete${capitalizedItemName}"), DeleteDataFetcher())

                if (it.versioned) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "purge${capitalizedItemName}"), PurgeDataFetcher())
                }

                if (!it.notLockable) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "lock${capitalizedItemName}"), lockDataFetcher)

                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "unlock${capitalizedItemName}"), unlockDataFetcher)
                }

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "promote${capitalizedItemName}"), PromoteDataFetcher())

                // Custom methods
                if (it.implementation != null) {
                    val customMethodNames = dataEngine.getCustomMethods(it.name)
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
            .filter { (_, attribute) -> attribute.type == Type.relation }
            .forEach { (attrName, attribute) ->
                if (attribute.relType == RelType.oneToOne || attribute.relType == RelType.manyToOne) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(capitalizedItemName, attrName), relationResponseDataFetcher)
                } else if (attribute.isCollection()) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(capitalizedItemName, attrName), relationResponseCollectionDataFetcher)
                }
            }
    }

    companion object {
        private const val QUERY_TYPE = "Query"
        private const val MUTATION_TYPE = "Mutation"

        private val excludeItemPolicy = ExcludeItemPolicy()
    }
}