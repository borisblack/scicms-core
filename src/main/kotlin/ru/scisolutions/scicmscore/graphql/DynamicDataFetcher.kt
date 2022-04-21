package ru.scisolutions.scicmscore.graphql

import com.netflix.graphql.dgs.DgsCodeRegistry
import com.netflix.graphql.dgs.DgsComponent
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import graphql.schema.idl.TypeDefinitionRegistry

@DgsComponent
class DynamicDataFetcher {
    @DgsCodeRegistry
    fun registry(codeRegistryBuilder: GraphQLCodeRegistry.Builder, registry: TypeDefinitionRegistry): GraphQLCodeRegistry.Builder {
        // codeRegistryBuilder
        //     .dataFetcher(FieldCoordinates.coordinates("Query", "queryItem"), dataFetchers.getCustomDataFetcher())
        //     .dataFetcher(FieldCoordinates.coordinates("Mutation", "mutateItem"), dataFetchers.getCustomDataFetcher())

        return codeRegistryBuilder
    }
}