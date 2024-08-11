package ru.scisolutions.scicmscore.engine.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonInclude(Include.NON_NULL)
class ListPagination<T>(
    val list: List<T>,
    page: Int? = null,
    pageSize: Int? = null,
    start: Int? = null,
    limit: Int? = null,
    total: Int?,
    pageCount: Int?,
) : Pagination(page, pageSize, start, limit, total, pageCount) {
    fun toBasePagination() = Pagination(page, pageSize, start, limit, total, pageCount)
}
