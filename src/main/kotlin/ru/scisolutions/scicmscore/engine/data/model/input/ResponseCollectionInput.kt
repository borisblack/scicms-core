package ru.scisolutions.scicmscore.engine.data.model.input

class ResponseCollectionInput(
    val filters: ItemFiltersInput?,
    val pagination: PaginationInput?,
    val sort: List<String>?,
    val majorRev: String?,
    val locale: String?
)