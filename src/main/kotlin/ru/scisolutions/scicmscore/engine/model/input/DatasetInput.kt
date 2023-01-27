package ru.scisolutions.scicmscore.engine.model.input

class DatasetInput(
    val filters: DatasetFilterInput?,
    val fields: List<String>?,
    val pagination: PaginationInput?,
    val sort: List<String>?
)