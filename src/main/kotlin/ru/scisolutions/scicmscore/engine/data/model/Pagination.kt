package ru.scisolutions.scicmscore.engine.data.model

class Pagination(
    val page: Int? = null,
    val pageSize: Int? = null,
    val start: Int? = null,
    val limit: Int? = null,
    val total: Int,
    val pageCount: Int
)