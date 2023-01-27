package ru.scisolutions.scicmscore.engine.mapper

import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput
import ru.scisolutions.scicmscore.model.AggregateType

class DatasetInputMapper() {
    fun map(arguments: Map<String, Any?>): DatasetInput {
        val datasetFiltersMap = arguments[FILTERS_ARG_NAME] as Map<String, Any>?
        val fields = arguments[FIELDS_ARG_NAME] as List<String>?
        val paginationMap = arguments[PAGINATION_ARG_NAME] as Map<String, Int>?
        val sort = arguments[SORT_ARG_NAME] as List<String>?
        val aggregate = arguments[AGGREGATE_ARG_NAME] as String?
        val aggregateField = arguments[AGGREGATE_FIELD_ARG_NAME] as String?
        val groupField = arguments[GROUP_FIELD_ARG_NAME] as String?

        return DatasetInput(
            filters = datasetFiltersMap?.let { datasetFiltersInputMapper.map(it) },
            fields = fields,
            pagination = paginationMap?.let { PaginationInput.fromMap(it) },
            sort = sort,
            aggregate = if (aggregate == null) null else AggregateType.valueOf(aggregate),
            aggregateField = aggregateField,
            groupField = groupField
        )
    }
    companion object {
        const val FILTERS_ARG_NAME = "filters"
        const val FIELDS_ARG_NAME = "fields"
        const val PAGINATION_ARG_NAME = "pagination"
        const val SORT_ARG_NAME = "sort"
        const val AGGREGATE_ARG_NAME = "aggregate"
        const val AGGREGATE_FIELD_ARG_NAME = "aggregateField"
        const val GROUP_FIELD_ARG_NAME = "groupField"

        private val datasetFiltersInputMapper = DatasetFiltersInputMapper()
    }
}