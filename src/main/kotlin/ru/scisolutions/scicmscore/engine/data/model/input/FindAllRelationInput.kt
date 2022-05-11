package ru.scisolutions.scicmscore.engine.data.model.input

open class FindAllRelationInput(
    val filters: ItemFiltersInput?,
    val pagination: PaginationInput?,
    val sort: List<String>?
)