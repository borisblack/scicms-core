package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.DatasourceType
import ru.scisolutions.scicmscore.engine.model.input.DatasourceTablesInput
import ru.scisolutions.scicmscore.engine.model.response.DatasourceTablesResponse
import ru.scisolutions.scicmscore.engine.model.response.Pagination
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.engine.persistence.dao.DatasourceDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Datasource
import ru.scisolutions.scicmscore.engine.persistence.service.DatasourceService
import ru.scisolutions.scicmscore.engine.util.Acl

@Service
class DatasourceHandler(
    private val datasourceService: DatasourceService,
    private val datasourceDao: DatasourceDao
) {
    fun loadTables(datasourceName: String, input: DatasourceTablesInput): DatasourceTablesResponse {
        val isMainDataSource = datasourceName == Datasource.MAIN_DATASOURCE_NAME
        val datasource = if (isMainDataSource) null else datasourceService.findByNameForRead(datasourceName)
        if ((isMainDataSource && (Acl.getRoles() intersect mainDatasourceRoles).isNotEmpty()) || datasource != null) {
            when (datasource?.sourceType) {
                DatasourceType.SPREADSHEET -> {
                    return datasourceDao.loadExcelTables(datasourceName, input)
                }

                DatasourceType.CSV -> {
                    TODO("CSV file processing")
                }

                else -> {
                    return datasourceDao.loadTables(datasourceName, input)
                }
            }
        }

        logger.warn("Datasource [$datasourceName] not found.")
        return DatasourceTablesResponse(
            data = emptyList(),
            meta =
            ResponseCollectionMeta(
                pagination =
                Pagination(
                    total = 0,
                    pageCount = 0
                )
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasourceHandler::class.java)
        private val mainDatasourceRoles =
            setOf(
                Acl.ROLE_ADMIN,
                Acl.ROLE_ANALYST
            )
    }
}
