package ru.scisolutions.scicmscore.engine.data.handler.impl

import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.db.ItemRecMapper
import ru.scisolutions.scicmscore.engine.data.db.query.FilterConditionBuilder
import ru.scisolutions.scicmscore.engine.data.db.query.LocaleConditionBuilder
import ru.scisolutions.scicmscore.engine.data.db.query.VersionConditionBuilder
import ru.scisolutions.scicmscore.engine.data.handler.ResponseCollectionHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.ResponseCollectionInput
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.util.AccessUtil

@Service
class ResponseCollectionHandlerImpl(
    private val itemService: ItemService,
    private val filterConditionBuilder: FilterConditionBuilder,
    private val localeConditionBuilder: LocaleConditionBuilder,
    private val versionConditionBuilder: VersionConditionBuilder,
    private val jdbcTemplate: JdbcTemplate
) : ResponseCollectionHandler {
    override fun getResponseCollection(itemName: String, input: ResponseCollectionInput, selectAttrNames: Set<String>): ResponseCollection {
        val item = itemService.getItemOrThrow(itemName)
        val itemRecList = findAll(item, selectAttrNames, input)

        return ResponseCollection(itemRecList)
    }

    private fun findAll(item: Item, selectAttrNames: Set<String>, input: ResponseCollectionInput): List<ItemRec> {
        val sql = buildFindAllSql(item, selectAttrNames, input)

        logger.debug("Running SQL: {}", sql)
        val itemRecList: List<ItemRec> = jdbcTemplate.query(sql, ItemRecMapper(item))

        return itemRecList
    }

    private fun buildFindAllSql(item: Item, selectAttrNames: Set<String>, input: ResponseCollectionInput): String {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val permissionIdCol = DbColumn(table, PERMISSION_ID_COL_NAME, null, null)
        val columns = selectAttrNames
            .map {
                val attribute = item.spec.getAttributeOrThrow(it)
                DbColumn(table, attribute.columnName ?: it.lowercase(), null, null)
            }
            .toTypedArray()

        val query = SelectQuery()
            .addColumns(*columns)

        // Version
        val versionCondition = versionConditionBuilder.newVersionCondition(table, item, input.majorRev)
        if (versionCondition != null)
            query.addCondition(versionCondition)

        // Locale
        val localeCondition = localeConditionBuilder.newLocaleCondition(table, item, input.locale)
        if (localeCondition != null)
            query.addCondition(localeCondition)

        // Filters
        if (input.filters != null) {
            query.addCondition(
                filterConditionBuilder.newFilterCondition(schema, table, query, item, input.filters)
            )
        }

        return query
            .addCondition(
                InCondition(permissionIdCol, CustomSql(AccessUtil.getPermissionIdsForReadStatement()))
            )
            .validate().toString()
    }

    companion object {
        private const val PERMISSION_ID_COL_NAME = "permission_id"
        private const val IS_CURRENT_COL_NAME = "is_current"
        private const val MAJOR_REV_COL_NAME = "major_rev"
        private const val LOCALE_COL_NAME = "locale"
        private const val ALL = "all"

        private val logger = LoggerFactory.getLogger(ResponseCollectionHandlerImpl::class.java)
    }
}