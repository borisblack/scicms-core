package ru.scisolutions.scicmscore.engine.mapper

import ru.scisolutions.scicmscore.engine.model.input.DatasetFilterInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput

class DatasetInputMapper() {
    fun map(arguments: Map<String, Any?>): DatasetInput {
        val itemFiltersMap = arguments[FILTERS_ARG_NAME] as Map<String, Any>?
        val paginationMap = arguments[PAGINATION_ARG_NAME] as Map<String, Int>?
        val sort = arguments[SORT_ARG_NAME] as List<String>?

        return DatasetInput(
            filters = itemFiltersMap?.let { DatasetFilterInput.fromMap(it) },
            pagination = paginationMap?.let { PaginationInput.fromMap(it) },
            sort = sort
        )
    }
    companion object {
        const val FILTERS_ARG_NAME = "filters"
        const val PAGINATION_ARG_NAME = "pagination"
        const val SORT_ARG_NAME = "sort"
    }
}