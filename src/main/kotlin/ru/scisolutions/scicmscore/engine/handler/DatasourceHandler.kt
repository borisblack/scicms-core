package ru.scisolutions.scicmscore.engine.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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
    private val datasourceDao: DatasourceDao,
) {
    fun loadTables(datasource: String, input: DatasourceTablesInput): DatasourceTablesResponse {
        if ((datasource == Datasource.MAIN_DATASOURCE_NAME && (Acl.getRoles() intersect mainDatasourceRoles).isNotEmpty()) ||
            datasourceService.findByNameForRead(datasource) != null
        ) {
            return datasourceDao.loadTables(datasource, input)
        }

        logger.warn("Datasource [$datasource] not found.")
        return DatasourceTablesResponse(
            data = emptyList(),
            meta =
            ResponseCollectionMeta(
                pagination =
                Pagination(
                    total = 0,
                    pageCount = 0,
                ),
            ),
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasourceHandler::class.java)
        private val mainDatasourceRoles =
            setOf(
                Acl.ROLE_ADMIN,
                Acl.ROLE_ANALYST,
            )
    }
}
