package ru.scisolutions.scicmscore.engine.data.db

import com.healthmarketscience.sqlbuilder.SelectQuery
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.data.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.data.model.response.Pagination
import kotlin.math.ceil

@Component
class Paginator(
    private val dataProps: DataProps,
    private val jdbcTemplate: JdbcTemplate
) {
    fun paginate(query: SelectQuery, paginationInput: PaginationInput?, selectPaginationFields: Set<String>): Pagination {
        var total: Int? = null
        if (TOTAL_FIELD_NAME in selectPaginationFields || PAGE_COUNT_FIELD_NAME in selectPaginationFields) {
            total = fetchTotal(query.toString())
        }

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

    private fun fetchTotal(selectStatement: String): Int {
        val countSQL = "SELECT COUNT(*) FROM ($selectStatement) t"
        return jdbcTemplate.queryForObject(countSQL, Int::class.java) as Int
    }

    companion object {
        private const val TOTAL_FIELD_NAME = "total"
        private const val PAGE_COUNT_FIELD_NAME = "pageCount"
    }
}