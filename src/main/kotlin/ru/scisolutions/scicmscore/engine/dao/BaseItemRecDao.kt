package ru.scisolutions.scicmscore.engine.dao

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import ru.scisolutions.scicmscore.engine.db.mapper.ItemRecMapper
import ru.scisolutions.scicmscore.engine.db.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.engine.service.ItemCacheManager
import ru.scisolutions.scicmscore.persistence.entity.Item

open class BaseItemRecDao(
    private val dsManager: DatasourceManager,
    private val itemCacheManager: ItemCacheManager
) {
    fun findOne(item: Item, sql: String, paramSource: AttributeSqlParameterSource): ItemRec? =
        itemCacheManager.get(item, sql, paramSource) {
            logger.trace("Running SQL: {}", sql)
            if (paramSource.parameterNames.isNotEmpty()) {
                logger.trace(
                    "Binding parameters: {}",
                    paramSource.parameterNames.joinToString { "$it = ${paramSource.getValue(it)}" }
                )
            }

            try {
                dsManager.template(item.ds).queryForObject(sql, paramSource, ItemRecMapper(item))
            } catch (e: EmptyResultDataAccessException) {
                null
            }
        }

    fun count(item: Item, sql: String, paramSource: AttributeSqlParameterSource): Int {
        val countSQL = "SELECT COUNT(*) FROM ($sql) t"

        return itemCacheManager.get(item, countSQL, paramSource) {
            logger.trace("Running SQL: {}", countSQL)
            if (paramSource.parameterNames.isNotEmpty()) {
                logger.trace(
                    "Binding parameters: {}",
                    paramSource.parameterNames.joinToString { "$it = ${paramSource.getValue(it)}" }
                )
            }

            dsManager.template(item.ds).queryForObject(countSQL, paramSource, Int::class.java) as Int
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BaseItemRecDao::class.java)
    }
}