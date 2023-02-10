package ru.scisolutions.scicmscore.engine.dao.impl

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import ru.scisolutions.scicmscore.config.PersistenceConfig.JdbcTemplateMap
import ru.scisolutions.scicmscore.engine.db.mapper.ItemRecMapper
import ru.scisolutions.scicmscore.engine.db.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

open class BaseItemRecDao(private val jdbcTemplateMap: JdbcTemplateMap) {
    fun findOne(item: Item, sql: String, paramSource: AttributeSqlParameterSource): ItemRec? {
        logger.debug("Running SQL: {}", sql)
        val itemRec: ItemRec? =
            try {
                jdbcTemplateMap.getOrThrow(item.dataSource).queryForObject(sql, paramSource, ItemRecMapper(item))
            } catch (e: EmptyResultDataAccessException) {
                null
            }

        return itemRec
    }

    fun count(item: Item, sql: String, paramSource: AttributeSqlParameterSource): Int {
        val countSQL = "SELECT COUNT(*) FROM ($sql) t"
        logger.debug("Running SQL: {}", countSQL)
        return jdbcTemplateMap.getOrThrow(item.dataSource).queryForObject(countSQL, paramSource, Int::class.java) as Int
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BaseItemRecDao::class.java)
    }
}