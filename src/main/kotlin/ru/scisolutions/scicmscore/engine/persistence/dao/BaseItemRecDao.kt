package ru.scisolutions.scicmscore.engine.persistence.dao

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.mapper.ItemRecMapper
import ru.scisolutions.scicmscore.engine.persistence.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.engine.service.ItemCacheManager
import java.sql.DatabaseMetaData

open class BaseItemRecDao(
    private val dsManager: DatasourceManager,
    private val itemCacheManager: ItemCacheManager
) {
    protected open val logger: Logger = LoggerFactory.getLogger(BaseItemRecDao::class.java)

    fun findOne(item: Item, sql: String, paramSource: AttributeSqlParameterSource): ItemRec? =
        itemCacheManager.get(item, sql, paramSource) {
            traceSqlAndParameters(sql, paramSource)
            try {
                dsManager.template(item.ds).queryForObject(sql, paramSource, ItemRecMapper(item))
            } catch (e: EmptyResultDataAccessException) {
                null
            }
        }

    fun count(item: Item, sql: String, paramSource: AttributeSqlParameterSource): Int {
        val countSQL = "SELECT COUNT(*) FROM ($sql) t"

        return itemCacheManager.get(item, countSQL, paramSource) {
            traceSqlAndParameters(countSQL, paramSource)
            dsManager.template(item.ds).queryForObject(countSQL, paramSource, Int::class.java) as Int
        }
    }

    protected fun traceSqlAndParameters(sql: String, paramSource: AttributeSqlParameterSource) {
        logger.trace("Running SQL: {}", sql)
        if (paramSource.parameterNames.isNotEmpty()) {
            logger.trace(
                "Binding parameters: {}",
                paramSource.parameterNames.joinToString { "$it = ${paramSource.getValue(it)}" }
            )
        }
    }

    fun dbMetaData(item: Item): DatabaseMetaData = dsManager.databaseMetaData(item.ds)
}
