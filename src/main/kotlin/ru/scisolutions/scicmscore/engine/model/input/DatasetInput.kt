package ru.scisolutions.scicmscore.engine.model.input

import ru.scisolutions.scicmscore.engine.model.AggregateType

class DatasetInput(
    val filters: DatasetFiltersInput?,
    val fields: List<DatasetFieldInput>?,
    val pagination: PaginationInput?,
    val sort: List<String>?,
    val aggregate: AggregateType?,
    val aggregateField: String?,
    val groupFields: List<String>?
)