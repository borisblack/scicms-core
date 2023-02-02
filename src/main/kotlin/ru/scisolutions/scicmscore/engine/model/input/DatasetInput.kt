package ru.scisolutions.scicmscore.engine.model.input

import ru.scisolutions.scicmscore.model.AggregateType

class DatasetInput(
    val filters: DatasetFiltersInput?,
    val fields: List<String>?,
    val pagination: PaginationInput?,
    val sort: List<String>?,
    val aggregate: AggregateType?,
    val aggregateField: String?,
    val groupField: String?
)