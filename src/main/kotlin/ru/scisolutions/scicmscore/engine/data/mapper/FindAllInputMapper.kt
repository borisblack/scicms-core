package ru.scisolutions.scicmscore.engine.data.mapper

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.data.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.data.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.data.model.input.FindAllInput

@Component
class FindAllInputMapper(private val itemFiltersInputMapper: ItemFiltersInputMapper) {
    fun mapToResponseCollectionInput(itemName: String, arguments: Map<String, Any>): FindAllInput {
        val itemFiltersMap = arguments[FILTERS_ARG_NAME] as Map<String, Any>?
        val paginationMap = arguments[PAGINATION_ARG_NAME] as Map<String, Int?>?
        val sort = arguments[SORT_ARG_NAME] as List<String>?
        val majorRev = arguments[MAJOR_REV_ARG_NAME] as String?
        val locale = arguments[LOCALE_ARG_NAME] as String?
        val state = arguments[STATE_ARG_NAME] as String?

        return FindAllInput(
            filters = itemFiltersMap?.let { itemFiltersInputMapper.map(itemName, it) },
            pagination = paginationMap?.let { PaginationInput.fromMap(it) },
            sort = sort,
            majorRev = majorRev,
            locale = locale,
            state = state
        )
    }

    fun mapToRelationResponseCollectionInput(itemName: String, arguments: Map<String, Any>): FindAllRelationInput {
        val itemFiltersMap = arguments[FILTERS_ARG_NAME] as Map<String, Any>?
        val paginationMap = arguments[PAGINATION_ARG_NAME] as Map<String, Int?>?
        val sort = arguments[SORT_ARG_NAME] as List<String>?

        return FindAllRelationInput(
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
        const val LOCALE_ARG_NAME = "locale"
        const val STATE_ARG_NAME = "state"
    }
}