package ru.scisolutions.scicmscore.engine.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.persistence.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.persistence.query.DatasetQueryBuilder
import ru.scisolutions.scicmscore.engine.persistence.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.DatasetRec
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.CacheStatistic
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.engine.persistence.service.DatasetService

@Service
class DatasetHandler(
    private val datasetService: DatasetService,
    private val datasetQueryBuilder: DatasetQueryBuilder,
    private val datasetDao: DatasetDao
) {
    fun load(datasetName: String, input: DatasetInput): DatasetResponse {
        val dataset = datasetService.findByNameForRead(datasetName)
            ?: return DatasetResponse()

        val paramSource = DatasetSqlParameterSource()
        val loadQuery = datasetQueryBuilder.buildLoadQuery(dataset, input, paramSource)
        val res: CacheStatistic<List<DatasetRec>> = datasetDao.load(dataset, loadQuery.sql, paramSource)

        return DatasetResponse(
            data = res.result,
            query = loadQuery.sql,
            params = paramSource.parameterNames.associateWith { paramSource.getValue(it) },
            timeMs = res.timeMs,
            cacheHit = res.cacheHit,
            meta = ResponseCollectionMeta(
                pagination = loadQuery.pagination
            )
        )
    }
}