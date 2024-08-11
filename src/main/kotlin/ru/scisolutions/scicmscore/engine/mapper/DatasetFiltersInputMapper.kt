package ru.scisolutions.scicmscore.engine.mapper

import ru.scisolutions.scicmscore.engine.model.input.AbstractFilterInput.Companion.AND_KEY
import ru.scisolutions.scicmscore.engine.model.input.AbstractFilterInput.Companion.NOT_KEY
import ru.scisolutions.scicmscore.engine.model.input.AbstractFilterInput.Companion.OR_KEY
import ru.scisolutions.scicmscore.engine.model.input.DatasetFiltersInput
import ru.scisolutions.scicmscore.engine.model.input.PrimitiveFilterInput

class DatasetFiltersInputMapper {
    fun map(datasetFiltersMap: Map<String, Any>, opPrefix: String = ""): DatasetFiltersInput {
        val datasetFiltersMapOfMaps = datasetFiltersMap.filterValues { it is Map<*, *> } as Map<String, Map<String, Any>>

        val fieldFilters: Map<String, PrimitiveFilterInput> =
            datasetFiltersMapOfMaps
                .filterKeys { it !in excludedKeys }
                .mapValues { (_, filterValue) ->
                    PrimitiveFilterInput.fromMap(filterValue, opPrefix)
                }

        val datasetFiltersMapOfLists = datasetFiltersMap.filterValues { it is List<*> } as Map<String, List<Any>>
        val andFiltersList =
            datasetFiltersMapOfLists["${opPrefix}$AND_KEY"]?.let { list ->
                list.filterIsInstance<Map<*, *>>()
                    .map { map(it as Map<String, Any>, opPrefix) } // recursive
            }

        val orFiltersList =
            datasetFiltersMapOfLists["${opPrefix}$OR_KEY"]?.let { list ->
                list.filterIsInstance<Map<*, *>>()
                    .map { map(it as Map<String, Any>, opPrefix) } // recursive
            }

        val notFilters = datasetFiltersMapOfMaps["${opPrefix}$NOT_KEY"]?.let { map(it, opPrefix) } // recursive

        return DatasetFiltersInput(
            fieldFilters = fieldFilters,
            andFiltersList = andFiltersList,
            orFiltersList = orFiltersList,
            notFilters = notFilters
        )
    }

    companion object {
        private val excludedKeys = setOf(AND_KEY, OR_KEY, NOT_KEY)
    }
}
