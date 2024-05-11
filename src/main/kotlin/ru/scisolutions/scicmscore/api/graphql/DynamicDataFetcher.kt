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
import ru.scisolutions.scicmscore.api.graphql.datafetcher.query.FindAllDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.query.FindAllRelatedDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.query.FindOneDataFetcher
import ru.scisolutions.scicmscore.api.graphql.datafetcher.query.FindOneRelatedDataFetcher
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.Attribute.RelType
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.extension.upperFirst

@DgsComponent
class DynamicDataFetcher(
    private val itemService: ItemService,
    private val engine: Engine,
    private val findOneDataFetcher: FindOneDataFetcher,
    private val findOneRelatedDataFetcher: FindOneRelatedDataFetcher,
    private val findAllDataFetcher: FindAllDataFetcher,
    private val findAllRelatedDataFetcher: FindAllRelatedDataFetcher,
    private val createDataFetcher: CreateDataFetcher,
    private val createVersionDataFetcher: CreateVersionDataFetcher,
    private val createLocalizationDataFetcher: CreateLocalizationDataFetcher,
    private val updateDataFetcher: UpdateDataFetcher,
    private val deleteDataFetcher: DeleteDataFetcher,
    private val purgeDataFetcher: PurgeDataFetcher,
    private val lockDataFetcher: LockDataFetcher,
    private val unlockDataFetcher: UnlockDataFetcher,
    private val promoteDataFetcher: PromoteDataFetcher,
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
                    .dataFetcher(FieldCoordinates.coordinates(QUERY_TYPE, it.name), findOneDataFetcher)
                    .dataFetcher(FieldCoordinates.coordinates(QUERY_TYPE, it.pluralName), findAllDataFetcher)
            }

        // Mutation
        items.asSequence()
            .filter { !it.readOnly }
            .filter { !excludeItemPolicy.excludeFromMutation(it) }
            .forEach {
                val capitalizedItemName = it.name.upperFirst()

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
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "delete${capitalizedItemName}"), deleteDataFetcher)

                if (it.versioned) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "purge${capitalizedItemName}"), purgeDataFetcher)
                }

                if (!it.notLockable) {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "lock${capitalizedItemName}"), lockDataFetcher)

                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "unlock${capitalizedItemName}"), unlockDataFetcher)
                }

                codeRegistryBuilder
                    .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "promote${capitalizedItemName}"), promoteDataFetcher)

                // Custom methods
                if (it.implementation != null) {
                    val customMethodNames = engine.getCustomMethods(it.name)
                    for (methodName in customMethodNames) {
                        codeRegistryBuilder
                            .dataFetcher(FieldCoordinates.coordinates(MUTATION_TYPE, "${methodName}${capitalizedItemName}"), customMethodDataFetcher)
                    }
                }
            }

        return codeRegistryBuilder
    }

    private fun addAttributeDataFetchers(codeRegistryBuilder: GraphQLCodeRegistry.Builder, item: Item) {
        val capitalizedItemName = item.name.upperFirst()
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> attribute.type == FieldType.relation || attribute.type == FieldType.media }
            .forEach { (attrName, attribute) ->
                if (attribute.type == FieldType.relation) {
                    if (attribute.relType == RelType.oneToOne || attribute.relType == RelType.manyToOne) {
                        codeRegistryBuilder
                            .dataFetcher(FieldCoordinates.coordinates(capitalizedItemName, attrName), findOneRelatedDataFetcher)
                    } else if (attribute.isCollection()) {
                        codeRegistryBuilder
                            .dataFetcher(FieldCoordinates.coordinates(capitalizedItemName, attrName), findAllRelatedDataFetcher)
                    }
                } else {
                    codeRegistryBuilder
                        .dataFetcher(FieldCoordinates.coordinates(capitalizedItemName, attrName), findOneRelatedDataFetcher)
                }
            }
    }

    companion object {
        private const val QUERY_TYPE = "Query"
        private const val MUTATION_TYPE = "Mutation"

        private val excludeItemPolicy = ExcludeItemPolicy()
    }
}