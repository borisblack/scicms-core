package ru.scisolutions.scicmscore.engine.persistence.paginator

import com.healthmarketscience.sqlbuilder.SelectQuery
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput
import ru.scisolutions.scicmscore.engine.model.response.CacheStatistic
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.engine.persistence.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset
import ru.scisolutions.scicmscore.engine.persistence.query.DatasetSqlParameterSource

@Component
class DatasetPaginator(
    dataProps: DataProps,
    private val datasetDao: DatasetDao
) : AbstractPaginator(dataProps) {
    fun paginate(dataset: Dataset, paginationInput: PaginationInput, query: SelectQuery, paramSource: DatasetSqlParameterSource): Pagination {
        val total: CacheStatistic<Int> = datasetDao.count(dataset, query.toString(), paramSource)
        return paginate(
            dbMetaData = datasetDao.dbMetaData(dataset),
            paginationInput = paginationInput,
            query = query,
            total = total.result
        ).apply {
            this.timeMs = total.timeMs
            this.cacheHit = total.cacheHit
        }
    }
}
