package ru.scisolutions.scicmscore.engine.db.paginator

import com.healthmarketscience.sqlbuilder.SelectQuery
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.persistence.entity.Dataset

@Component
class DatasetPaginator(
    dataProps: DataProps,
    private val datasetDao: DatasetDao
) : AbstractPaginator(dataProps) {
    fun paginate(
        dataset: Dataset,
        paginationInput: PaginationInput,
        query: SelectQuery,
        paramSource: DatasetSqlParameterSource
    ): Pagination {
        val total: Int = datasetDao.count(dataset, query.toString(), paramSource)
        return paginate(paginationInput, query, total)
    }
}