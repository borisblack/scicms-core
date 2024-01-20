package ru.scisolutions.scicmscore.engine.dao

import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.db.mapper.ColumnsMapper
import ru.scisolutions.scicmscore.engine.db.mapper.TablesMapper
import ru.scisolutions.scicmscore.engine.model.input.DatasourceTablesInput
import ru.scisolutions.scicmscore.engine.model.response.DatasourceTablesResponse
import ru.scisolutions.scicmscore.engine.service.DatasourceManager

@Service
class DatasourceDao(
    dataProps: DataProps,
    private val dsManager: DatasourceManager
) {
    private val tablesMapper = TablesMapper(dataProps)

    fun loadMetaData(datasource: String, queryString: String): SqlRowSetMetaData {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = schema.addTable(queryString)
        val query = SelectQuery()
            .addAllColumns()
            .addFromTable(table)
            .setFetchNext(1)
            .validate()

        val jdbcTemplate = dsManager.template(datasource)
        val sql = query.toString()
        logger.debug("Running loadMetaData SQL: {}", sql)
        return jdbcTemplate.queryForRowSet(sql, MapSqlParameterSource()).metaData
    }

    fun loadTables(datasource: String, input: DatasourceTablesInput): DatasourceTablesResponse {
        val dataSource = dsManager.dataSource(datasource)
        dataSource.connection.use {
            val metaData = it.metaData

            logger.debug("Fetching metaData tables")
            val tablesResultSet = metaData.getTables(
                it.catalog,
                if (input.schema.isNullOrBlank()) it.schema else input.schema,
                null,
                arrayOf("TABLE")
            )
            logger.debug("Fetched metaData tables.")

            val response = tablesMapper.map(tablesResultSet, input) { tableName ->
                columnsMapper.map(loadMetaData(datasource, tableName))
            }

            return response
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasourceDao::class.java)
        private val columnsMapper = ColumnsMapper()
    }
}