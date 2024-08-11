package ru.scisolutions.scicmscore.engine.persistence.paginator

import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.custom.mysql.MysLimitClause
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import java.sql.DatabaseMetaData
import kotlin.math.ceil

abstract class AbstractPaginator(private val dataProps: DataProps) {
    protected fun paginate(dbMetaData: DatabaseMetaData, paginationInput: PaginationInput?, query: SelectQuery, total: Int?): Pagination {
        if (paginationInput != null) {
            if ((paginationInput.page != null || paginationInput.pageSize != null) && (paginationInput.start != null || paginationInput.limit != null)) {
                throw IllegalArgumentException("Pagination methods cannot be mixed. Use either page with pageSize or start with limit")
            }

            if (paginationInput.page != null || paginationInput.pageSize != null) {
                val page = paginationInput.page ?: 1
                if (page < 1) {
                    throw IllegalArgumentException("The page cannot be less than 1")
                }

                val pageSize = paginationInput.pageSize ?: dataProps.defaultLimit
                if (pageSize < 1) {
                    throw IllegalArgumentException("The pageSize cannot be less than 1")
                }

                if (pageSize > dataProps.maxLimit) {
                    throw IllegalArgumentException("The pageSize cannot be more than ${dataProps.maxLimit}")
                }

                val offset = (page - 1) * pageSize

                addPagination(
                    dbMetaData = dbMetaData,
                    query = query,
                    offset = offset,
                    rowCount = pageSize,
                )

                val pageCount: Int? = if (total == null) null else ceil(total.toDouble() / pageSize).toInt()

                return Pagination(
                    page = page,
                    pageSize = pageSize,
                    total = total,
                    pageCount = pageCount,
                )
            } else if (paginationInput.start != null || paginationInput.limit != null) {
                val start = paginationInput.start ?: 0
                if (start < 0) {
                    throw IllegalArgumentException("The start cannot be less than 0")
                }

                val limit = paginationInput.limit ?: dataProps.defaultLimit
                if (limit < 1) {
                    throw IllegalArgumentException("The limit cannot be less than 1")
                }

                if (limit > dataProps.maxLimit) {
                    throw IllegalArgumentException("The limit cannot be more than ${dataProps.maxLimit}")
                }

                addPagination(
                    dbMetaData = dbMetaData,
                    query = query,
                    offset = start,
                    rowCount = limit,
                )

                return Pagination(
                    start = start,
                    limit = limit,
                    total = total,
                    pageCount = null,
                )
            }
        }

        return Pagination(
            total = total,
            pageCount = null,
        )
    }

    private fun addPagination(dbMetaData: DatabaseMetaData, query: SelectQuery, offset: Int, rowCount: Int) {
        when (dbMetaData.databaseProductName) {
            "SQLite", "mysql" -> {
                query
                    .addCustomization(MysLimitClause(offset, rowCount))
            }

            else -> { // SQL 2008
                query
                    .setOffset(offset)
                    .setFetchNext(rowCount)
            }
        }
    }
}
