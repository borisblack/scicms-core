package ru.scisolutions.scicmscore.engine.data.mapper

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.data.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.data.model.input.RelationResponseCollectionInput
import ru.scisolutions.scicmscore.engine.data.model.input.ResponseCollectionInput

@Component
class ResponseCollectionInputMapper(private val itemFiltersInputMapper: ItemFiltersInputMapper) {
    fun mapToResponseCollectionInput(itemName: String, arguments: Map<String, Any>): ResponseCollectionInput {
        val itemFiltersMap = arguments[FILTERS_ARG_NAME] as Map<String, Any>?
        val paginationMap = arguments[PAGINATION_ARG_NAME] as Map<String, Int?>?
        val sort = arguments[SORT_ARG_NAME] as List<String>?
        val majorRev = arguments[MAJOR_REV_ARG_NAME] as String?
        val isReleased = arguments[RELEASED_ARG_NAME] as Boolean?
        val locale = arguments[LOCALE_ARG_NAME] as String?

        return ResponseCollectionInput(
            filters = itemFiltersMap?.let { itemFiltersInputMapper.map(itemName, it) },
            pagination = paginationMap?.let { PaginationInput.fromMap(it) },
            sort = sort,
            majorRev = majorRev,
            isReleased = isReleased,
            locale = locale
        )
    }

    fun mapToRelationResponseCollectionInput(itemName: String, arguments: Map<String, Any>): RelationResponseCollectionInput {
        val itemFiltersMap = arguments[FILTERS_ARG_NAME] as Map<String, Any>?
        val paginationMap = arguments[PAGINATION_ARG_NAME] as Map<String, Int?>?
        val sort = arguments[SORT_ARG_NAME] as List<String>?

        return RelationResponseCollectionInput(
            filters = itemFiltersMap?.let { itemFiltersInputMapper.map(itemName, it) },
            pagination = paginationMap?.let { PaginationInput.fromMap(it) },
            sort = sort
        )
    }

    companion object {
        const val FILTERS_ARG_NAME = "filters"
        const val PAGINATION_ARG_NAME = "pagination"
        const val SORT_ARG_NAME = "sort"
        const val MAJOR_REV_ARG_NAME = "majorRev"
        const val RELEASED_ARG_NAME = "released"
        const val LOCALE_ARG_NAME = "locale"
    }
}