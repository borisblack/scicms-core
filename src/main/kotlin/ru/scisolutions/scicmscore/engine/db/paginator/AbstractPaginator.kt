package ru.scisolutions.scicmscore.engine.db.paginator

import com.healthmarketscience.sqlbuilder.SelectQuery
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import kotlin.math.ceil

abstract class AbstractPaginator(private val dataProps: DataProps) {
    protected fun paginate(paginationInput: PaginationInput?, query: SelectQuery, total: Int?): Pagination {
        if (paginationInput != null) {
            if ((paginationInput.page != null || paginationInput.pageSize != null) && (paginationInput.start != null || paginationInput.limit != null))
                throw IllegalArgumentException("Pagination methods cannot be mixed. Use either page with pageSize or start with limit")

            if (paginationInput.page != null || ((paginationInput.pageSize ?: UNLIMITED_PAGE_SIZE) != UNLIMITED_PAGE_SIZE)) {
                val page = paginationInput.page ?: 1
                if (page < 1)
                    throw IllegalArgumentException("The page cannot be less than 1")

                val pageSize = paginationInput.pageSize ?: dataProps.defaultLimit
                if (pageSize < 1)
                    throw IllegalArgumentException("The pageSize cannot be less than 1")

                if (pageSize > dataProps.maxLimit)
                    throw IllegalArgumentException("The pageSize cannot be more than ${dataProps.maxLimit}")

                val offset = (page - 1) * pageSize
                query
                    .setOffset(offset)
                    .setFetchNext(pageSize)

                val pageCount: Int? = if (total == null) null else ceil(total.toDouble() / pageSize).toInt()

                return Pagination(
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

                query
                    .setOffset(start)
                    .setFetchNext(limit)

                return Pagination(
                    start = start,
                    limit = limit,
                    total = total,
                    pageCount = null
                )
            }
        }

        return Pagination(
            total = total,
            pageCount = null
        )
    }

    companion object {
        private const val UNLIMITED_PAGE_SIZE = -1
    }
}