package ru.scisolutions.scicmscore.engine.dao.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.PersistenceConfig.JdbcTemplateMap
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.db.DatasetRowMapper
import ru.scisolutions.scicmscore.engine.db.query.DatasetQueryBuilder
import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.persistence.entity.Dataset

@Service
class DatasetDaoImpl(
    private val jdbcTemplateMap: JdbcTemplateMap,
    private val datasetQueryBuilder: DatasetQueryBuilder
) : DatasetDao {
    override fun load(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): List<Map<String, Any?>> {
        logger.debug("Running SQL: {}", sql)
        return jdbcTemplateMap.getOrThrow(dataset.dataSource).query(sql, paramSource, DatasetRowMapper())
    }

    override fun count(dataset: Dataset, sql: String, paramSource: DatasetSqlParameterSource): Int {
        val countSQL = "SELECT COUNT(*) FROM ($sql) t"
        logger.debug("Running SQL: {}", countSQL)
        return jdbcTemplateMap.getOrThrow(dataset.dataSource).queryForObject(countSQL, paramSource, Int::class.java) as Int
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasetDaoImpl::class.java)
    }
}