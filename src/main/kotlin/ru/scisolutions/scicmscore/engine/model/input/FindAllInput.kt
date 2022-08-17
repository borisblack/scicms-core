package ru.scisolutions.scicmscore.engine.model.input

class FindAllInput(
    filters: ItemFiltersInput?,
    pagination: PaginationInput?,
    sort: List<String>?,
    val majorRev: String?,
    val locale: String?,
    val state: String?
) : FindAllRelationInput(filters, pagination, sort)