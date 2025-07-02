package ru.scisolutions.scicmscore.engine.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.DatasetRec
import ru.scisolutions.scicmscore.engine.model.DatasourceType
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.CacheStatistic
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.engine.persistence.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset
import ru.scisolutions.scicmscore.engine.persistence.query.DatasetQueryBuilder
import ru.scisolutions.scicmscore.engine.persistence.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.engine.persistence.service.DatasetService

@Service
class DatasetHandler(
    private val datasetService: DatasetService,
    private val datasetQueryBuilder: DatasetQueryBuilder,
    private val datasetDao: DatasetDao
) {
    fun load(datasetName: String, input: DatasetInput): DatasetResponse {
        val dataset = datasetService.findByNameForRead(datasetName) ?: return DatasetResponse()

        return when (dataset.datasource?.sourceType) {
            DatasourceType.SPREADSHEET -> {
                TODO("Excel file processing")
            }
            DatasourceType.CSV -> {
                TODO("CSV file processing")
            }
            else -> {
                load(dataset, input)
            }
        }
    }

    /**
     * Fetches data from DBMS.
     */
    private fun load(dataset: Dataset, input: DatasetInput): DatasetResponse {
        val sourceType = dataset.datasource?.sourceType
        if (sourceType != DatasourceType.DATABASE)
            throw IllegalArgumentException("Unsupported source type: ${sourceType?.name}.")

        val paramSource = DatasetSqlParameterSource()
        val loadQuery = datasetQueryBuilder.buildLoadQuery(dataset, input, paramSource)
        val res: CacheStatistic<List<DatasetRec>> = datasetDao.load(dataset, loadQuery.sql, paramSource)

        return DatasetResponse(
            data = res.result,
            query = loadQuery.sql,
            params = paramSource.parameterNames.associateWith { paramSource.getValue(it) },
            timeMs = res.timeMs,
            cacheHit = res.cacheHit,
            meta =
                ResponseCollectionMeta(
                    pagination = loadQuery.pagination
                )
        )
    }
}
