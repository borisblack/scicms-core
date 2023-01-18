package ru.scisolutions.scicmscore.engine.dao.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.PersistenceConfig.JdbcTemplateMap
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.db.DatasetRowMapper
import ru.scisolutions.scicmscore.engine.db.query.DatasetQueryBuilder
import ru.scisolutions.scicmscore.engine.db.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.model.AggregateType
import ru.scisolutions.scicmscore.persistence.entity.Dataset

@Service
class DatasetDaoImpl(private val jdbcTemplateMap: JdbcTemplateMap) : DatasetDao {
    override fun findAll(
        dataset: Dataset,
        start: String?,
        end: String?,
        aggregateType: AggregateType?,
        groupBy: String?
    ): List<Map<String, Any?>> {
        val paramSource = DatasetSqlParameterSource()
        val query = datasetQueryBuilder.buildFindAllQuery(dataset, start, end, aggregateType, groupBy, paramSource)
        val sql = query.toString()
        logger.debug("Running SQL: {}", sql)
        val jdbcTemplate = jdbcTemplateMap.getOrThrow(dataset.dataSource)

        return jdbcTemplate.query(sql, paramSource, DatasetRowMapper())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasetDaoImpl::class.java)
        private val datasetQueryBuilder = DatasetQueryBuilder()
    }
}