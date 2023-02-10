package ru.scisolutions.scicmscore.engine.db.paginator

import com.healthmarketscience.sqlbuilder.SelectQuery
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.db.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.persistence.entity.Item

@Component
class ItemPaginator(
    dataProps: DataProps,
    private val itemRecDao: ItemRecDao
) : AbstractPaginator(dataProps) {
    fun paginate(item: Item, paginationInput: PaginationInput?, selectPaginationFields: Set<String>, query: SelectQuery, paramSource: AttributeSqlParameterSource): Pagination {
        var total: Int? = null
        if (TOTAL_FIELD_NAME in selectPaginationFields || PAGE_COUNT_FIELD_NAME in selectPaginationFields) {
            total = itemRecDao.count(item, query.toString(), paramSource)
        }

        return paginate(paginationInput, query, total)
    }

    companion object {
        private const val TOTAL_FIELD_NAME = "total"
        private const val PAGE_COUNT_FIELD_NAME = "pageCount"
    }
}