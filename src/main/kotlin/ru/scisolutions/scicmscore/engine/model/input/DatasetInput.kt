package ru.scisolutions.scicmscore.engine.model.input

class DatasetInput(
    val filters: DatasetFiltersInput?,
    val fields: List<DatasetFieldInput>?,
    val pagination: PaginationInput?,
    val sort: List<String>?
)
