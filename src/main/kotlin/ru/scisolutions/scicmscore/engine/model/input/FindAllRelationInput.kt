package ru.scisolutions.scicmscore.engine.model.input

open class FindAllRelationInput(
    val filters: ItemFiltersInput?,
    val pagination: PaginationInput?,
    val sort: List<String>?,
)
