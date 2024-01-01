package ru.scisolutions.scicmscore.engine.mapper

import ru.scisolutions.scicmscore.engine.model.AggregateType
import ru.scisolutions.scicmscore.engine.model.input.DatasetFieldInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput

class DatasetInputMapper() {
    fun map(arguments: Map<String, Any?>, opPrefix: String = ""): DatasetInput {
        val datasetFiltersMap = arguments[FILTERS_ARG_NAME] as Map<String, Any>?
        val fields = arguments[FIELDS_ARG_NAME] as List<Map<String, Any>>?
        val paginationMap = arguments[PAGINATION_ARG_NAME] as Map<String, Int>?
        val sort = arguments[SORT_ARG_NAME] as List<String>?
        val aggregate = arguments[AGGREGATE_ARG_NAME] as String?
        val aggregateField = arguments[AGGREGATE_FIELD_ARG_NAME] as String?
        val groupFields = arguments[GROUP_FIELDS_ARG_NAME] as List<String>?

        return DatasetInput(
            filters = datasetFiltersMap?.let { datasetFiltersInputMapper.map(it, opPrefix) },
            fields = fields?.map { mapDatasetFieldInput(it) },
            pagination = paginationMap?.let { PaginationInput.fromMap(it) },
            sort = sort,
            aggregate = aggregate?.let { AggregateType.valueOf(it) },
            aggregateField = aggregateField,
            groupFields = groupFields
        )
    }

    private fun mapDatasetFieldInput(field: Map<String, Any>): DatasetFieldInput =
        DatasetFieldInput(
            name = field[FIELD_NAME_ARG_NAME] as String,
            source = field[FIELD_SOURCE_ARG_NAME] as String?,
            aggregate = (field[AGGREGATE_ARG_NAME] as String?)?.let { AggregateType.valueOf(it) }
        )

    companion object {
        const val FILTERS_ARG_NAME = "filters"
        const val FIELDS_ARG_NAME = "fields"
        const val PAGINATION_ARG_NAME = "pagination"
        const val SORT_ARG_NAME = "sort"
        const val AGGREGATE_ARG_NAME = "aggregate"
        const val AGGREGATE_FIELD_ARG_NAME = "aggregateField"
        const val GROUP_FIELDS_ARG_NAME = "groupFields"
        const val FIELD_NAME_ARG_NAME = "name"
        const val FIELD_SOURCE_ARG_NAME = "source"

        private val datasetFiltersInputMapper = DatasetFiltersInputMapper()
    }
}