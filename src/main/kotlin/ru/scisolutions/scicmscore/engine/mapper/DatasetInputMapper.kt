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

        return DatasetInput(
            filters = datasetFiltersMap?.let { datasetFiltersInputMapper.map(it, opPrefix) },
            fields = fields?.map { mapDatasetFieldInput(it) },
            pagination = paginationMap?.let { PaginationInput.fromMap(it) },
            sort = sort
        )
    }

    private fun mapDatasetFieldInput(field: Map<String, Any>): DatasetFieldInput = DatasetFieldInput(
        name = field[FIELD_NAME_ARG_NAME] as String,
        custom = (field[FIELD_CUSTOM_ARG_NAME] as String).toBoolean(),
        source = field[FIELD_SOURCE_ARG_NAME] as String?,
        aggregate = (field[AGGREGATE_ARG_NAME] as String?)?.let { AggregateType.valueOf(it) },
        formula = field[FIELD_FORMULA_ARG_NAME] as String?
    )

    companion object {
        const val FILTERS_ARG_NAME = "filters"
        const val FIELDS_ARG_NAME = "fields"
        const val PAGINATION_ARG_NAME = "pagination"
        const val SORT_ARG_NAME = "sort"
        const val AGGREGATE_ARG_NAME = "aggregate"

        const val FIELD_NAME_ARG_NAME = "name"
        const val FIELD_CUSTOM_ARG_NAME = "custom"
        const val FIELD_SOURCE_ARG_NAME = "source"
        const val FIELD_FORMULA_ARG_NAME = "formula"

        private val datasetFiltersInputMapper = DatasetFiltersInputMapper()
    }
}
