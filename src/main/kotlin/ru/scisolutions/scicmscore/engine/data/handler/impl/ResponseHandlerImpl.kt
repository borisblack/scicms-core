package ru.scisolutions.scicmscore.engine.data.handler.impl

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.data.db.ItemRecMapper
import ru.scisolutions.scicmscore.engine.data.handler.ResponseHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.ItemService
import ru.scisolutions.scicmscore.util.AccessUtil

@Service
class ResponseHandlerImpl(
    private val itemService: ItemService,
    private val jdbcTemplate: JdbcTemplate,
) : ResponseHandler {
    override fun getResponse(itemName: String, selectAttrNames: Set<String>, id: String): Response {
        val item = itemService.getItemOrThrow(itemName)
        val itemRec = findByKeyColumn(item, selectAttrNames, ID_COL_NAME, id)

        return Response(itemRec)
    }

    override fun getRelationResponse(itemName: String, selectAttrNames: Set<String>, sourceItemRec: ItemRec, attrName: String): RelationResponse {
        val keyColValue = sourceItemRec[attrName] as String?
        if (keyColValue == null) {
            logger.info("Field [$attrName] is null, so it cannot be fetched")
            return RelationResponse()
        }

        val item = itemService.getItemOrThrow(itemName)
        val attribute = item.spec.getAttributeOrThrow(attrName)
        val keyColName = extractKeyColName(attribute)
        val itemRec = findByKeyColumn(item, selectAttrNames, keyColName, keyColValue)

        return RelationResponse(itemRec)
    }

    private fun extractKeyColName(attribute: Attribute): String {
        val targetKeyAttrName = attribute.extractTargetKeyAttrName()
        return if (targetKeyAttrName == ID_ATTR_NAME) {
            ID_COL_NAME
        } else {
            val targetItemName = attribute.extractTarget()
            val targetItem = itemService.getItemOrThrow(targetItemName)
            val targetAttribute = targetItem.spec.getAttributeOrThrow(targetKeyAttrName)
            targetAttribute.columnName ?: targetKeyAttrName.lowercase()
        }
    }

    private fun findByKeyColumn(item: Item, selectFields: Set<String>, keyColName: String, keyColValue: String): ItemRec? {
        val sql = buildFindByKeyColumnSql(item, selectFields, keyColName, keyColValue)

        logger.debug("Running SQL: {}", sql)
        val itemRec: ItemRec? =
            try {
                jdbcTemplate.queryForObject(sql, ItemRecMapper(item))
            } catch (e: EmptyResultDataAccessException) {
                null
            }

        return itemRec
    }

    private fun buildFindByKeyColumnSql(item: Item, selectFields: Set<String>, keyColName: String, keyColValue: String): String {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val keyCol = DbColumn(table, keyColName, null, null)
        val permissionIdCol = DbColumn(table, PERMISSION_ID_COL_NAME, null, null)
        val columns = selectFields
            .map {
                val attribute = item.spec.getAttributeOrThrow(it)
                DbColumn(table, attribute.columnName ?: it.lowercase(), null, null)
            }
            .toTypedArray()

        return SelectQuery()
            .addColumns(*columns)
            .addCondition(BinaryCondition.equalTo(keyCol, keyColValue))
            .addCondition(
                InCondition(
                    permissionIdCol,
                    CustomSql(AccessUtil.getPermissionIdsForReadStatement())
                )
            )
            .validate().toString()
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private const val ID_COL_NAME = "id"
        private const val PERMISSION_ID_COL_NAME = "permission_id"

        private val logger = LoggerFactory.getLogger(ResponseHandlerImpl::class.java)
    }
}