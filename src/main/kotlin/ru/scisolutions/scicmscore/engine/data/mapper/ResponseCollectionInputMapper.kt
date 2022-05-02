package ru.scisolutions.scicmscore.engine.data.mapper

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.data.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.data.model.input.ResponseCollectionInput

@Component
class ResponseCollectionInputMapper(private val itemFiltersInputMapper: ItemFiltersInputMapper) {
    fun map(itemName: String, arguments: Map<String, Any>): ResponseCollectionInput {
        val itemFiltersMap = arguments["filters"] as Map<String, Any>?
        val paginationMap = arguments["pagination"] as Map<String, Int?>?
        val sort = arguments["sort"] as List<String>?

        return ResponseCollectionInput(
            filters = itemFiltersMap?.let { itemFiltersInputMapper.map(itemName, it) },
            pagination = paginationMap?.let { PaginationInput.fromMap(it) },
            sort = sort
        )
    }
}