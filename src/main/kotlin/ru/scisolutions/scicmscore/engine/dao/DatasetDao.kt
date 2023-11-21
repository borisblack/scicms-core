package ru.scisolutions.scicmscore.engine.dao

import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.db.mapper.ColumnsMapper
import ru.scisolutions.scicmscore.engine.db.mapper.DatasetRowMapper
import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.model.DatasetSpec
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import java.util.Objects

@Service
class DatasetDao(
    private val dsManager: DatasourceManager
) {
    fun load(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): List<Map<String, Any?>> {
        logger.debug("Running load SQL: {}", sql)
        return dsManager.template(dataset.datasource?.name).query(sql, paramSource, DatasetRowMapper())
    }

    fun count(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): Int {
        val countSQL = "SELECT COUNT(*) FROM ($sql) t"
        logger.debug("Running count SQL: {}", countSQL)
        return dsManager.template(dataset.datasource?.name).queryForObject(countSQL, paramSource, Int::class.java) as Int
    }

    fun actualizeSpec(dataset: Dataset): Boolean {
        val hash = Objects.hash(
            dataset.datasource?.name,
            dataset.getQueryOrThrow()
        ).toString()

        if (dataset.hash == hash) {
            logger.debug("Dataset has not changed. Skip actualizing")
            return false
        }

        logger.debug("Dataset has changed. Reloading meta")
        dataset.spec = DatasetSpec(
            columns = columnsMapper.map(dataset, loadMetaData(dataset))
        )
        dataset.hash = hash

        return true
    }

    private fun loadMetaData(dataset: Dataset): SqlRowSetMetaData {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = schema.addTable(dataset.getQueryOrThrow())
        val query = SelectQuery()
            .addAllColumns()
            .addFromTable(table)
            .setFetchNext(1)
            .validate()

        val jdbcTemplate = dsManager.template(dataset.datasource?.name)
        val sql = query.toString()
        logger.debug("Running loadMetaData SQL: {}", sql)
        return jdbcTemplate.queryForRowSet(sql, MapSqlParameterSource()).metaData
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasetDao::class.java)
        private val columnsMapper = ColumnsMapper()
    }
}