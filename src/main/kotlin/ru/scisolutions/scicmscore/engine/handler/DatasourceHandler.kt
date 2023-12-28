package ru.scisolutions.scicmscore.engine.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.dao.DatasourceDao
import ru.scisolutions.scicmscore.engine.model.input.DatasourceTablesInput
import ru.scisolutions.scicmscore.engine.model.response.DatasourceTablesResponse
import ru.scisolutions.scicmscore.persistence.service.DatasourceService

@Service
class DatasourceHandler(
    private val datasourceService: DatasourceService,
    private val datasourceDao: DatasourceDao
) {
    fun loadTables(datasource: String, input: DatasourceTablesInput): DatasourceTablesResponse {
        datasourceService.findByNameForRead(datasource)
            ?: return DatasourceTablesResponse(data = emptyList())

        return datasourceDao.loadTables(datasource, input)
    }
}