package ru.scisolutions.scicmscore.engine.data.model.input

class RelationResponseCollectionInput(
    val filters: ItemFiltersInput?,
    val pagination: PaginationInput?,
    val sort: List<String>?
)