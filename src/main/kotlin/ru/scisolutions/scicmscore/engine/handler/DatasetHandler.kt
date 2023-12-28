package ru.scisolutions.scicmscore.engine.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.db.query.DatasetQueryBuilder
import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.persistence.service.DatasetService

@Service
class DatasetHandler(
    private val datasetService: DatasetService,
    private val datasetQueryBuilder: DatasetQueryBuilder,
    private val datasetDao: DatasetDao
) {
    fun load(datasetName: String, input: DatasetInput): DatasetResponse {
        val dataset = datasetService.findByNameForRead(datasetName)
            ?: return DatasetResponse(data = emptyList())

        val paramSource = DatasetSqlParameterSource()
        val loadQuery = datasetQueryBuilder.buildLoadQuery(dataset, input, paramSource)
        val data: List<Map<String, Any?>> = datasetDao.load(dataset, loadQuery.sql, paramSource)

        return DatasetResponse(
            data = data,
            meta = ResponseCollectionMeta(
                pagination = loadQuery.pagination
            )
        )
    }
}