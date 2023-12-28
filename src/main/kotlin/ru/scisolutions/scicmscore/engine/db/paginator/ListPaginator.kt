package ru.scisolutions.scicmscore.engine.db.paginator

import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.model.response.ListPagination
import kotlin.math.ceil
import kotlin.math.min

class ListPaginator(private val dataProps: DataProps) {
    fun <T> paginate(paginationInput: PaginationInput?, list: List<T>): ListPagination<T> {
        val total = list.size
        if (paginationInput != null) {
            if ((paginationInput.page != null || paginationInput.pageSize != null) && (paginationInput.start != null || paginationInput.limit != null))
                throw IllegalArgumentException("Pagination methods cannot be mixed. Use either page with pageSize or start with limit")

            if (paginationInput.page != null || paginationInput.pageSize != null) {
                val page = paginationInput.page ?: 1
                if (page < 1)
                    throw IllegalArgumentException("The page cannot be less than 1")

                val pageSize = paginationInput.pageSize ?: dataProps.defaultLimit
                if (pageSize < 1)
                    throw IllegalArgumentException("The pageSize cannot be less than 1")

                if (pageSize > dataProps.maxLimit)
                    throw IllegalArgumentException("The pageSize cannot be more than ${dataProps.maxLimit}")

                val offset = (page - 1) * pageSize
                val pageCount: Int = ceil(total.toDouble() / pageSize).toInt()

                return ListPagination(
                    list = list.subList(offset, min(offset + pageSize, total)),
                    page = page,
                    pageSize = pageSize,
                    total = total,
                    pageCount = pageCount
                )
            } else if (paginationInput.start != null || paginationInput.limit != null) {
                val start = paginationInput.start ?: 0
                if (start < 0)
                    throw IllegalArgumentException("The start cannot be less than 0")

                val limit = paginationInput.limit ?: dataProps.defaultLimit
                if (limit < 1)
                    throw IllegalArgumentException("The limit cannot be less than 1")

                if (limit > dataProps.maxLimit)
                    throw IllegalArgumentException("The limit cannot be more than ${dataProps.maxLimit}")

                return ListPagination(
                    list = list.subList(start, min(start + limit, total)),
                    start = start,
                    limit = limit,
                    total = total,
                    pageCount = null
                )
            }
        }

        return ListPagination(
            list = list,
            total = total,
            pageCount = null
        )
    }
}