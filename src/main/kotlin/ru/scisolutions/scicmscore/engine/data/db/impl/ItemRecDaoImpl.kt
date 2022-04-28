package ru.scisolutions.scicmscore.engine.data.db.impl

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.db.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.util.AccessUtil
import ru.scisolutions.scicmscore.persistence.entity.Item

@Service
class ItemRecDaoImpl(
    private val jdbi: Jdbi
) : ItemRecDao {
    override fun findById(item: Item, id: String, fields: Set<String>): ItemRec? {
        val sql = buildFindByIdSql(item, id, fields)

        logger.debug("Running SQL: {}", sql)
        val itemRec: ItemRec? = jdbi.withHandleUnchecked {
            val result = it.createQuery(sql)
                .map(ItemRecMapper(item))
                .findOne().orElse(null)

            result
        }

        return itemRec
    }

    private fun buildFindByIdSql(item: Item, id: String, fields: Set<String>): String {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val idCol = DbColumn(table, ID_COL_NAME, null, null)
        val permissionIdCol = DbColumn(table, PERMISSION_ID_COL_NAME, null, null)
        val columns = fields
            .map { DbColumn(table, item.spec.getAttribute(it).columnName, null, null) }
            .toTypedArray()

        return SelectQuery()
            .addColumns(*columns)
            .addCondition(BinaryCondition.equalTo(idCol, id))
            .addCondition(
                InCondition(
                    permissionIdCol,
                    CustomSql(AccessUtil.getPermissionIdsStatement(AccessUtil.READ_MASK))
                )
            )
            .validate().toString()
    }

    companion object {
        private const val ID_COL_NAME = "id"
        private const val PERMISSION_ID_COL_NAME = "permission_id"

        private val logger = LoggerFactory.getLogger(ItemRecDao::class.java)
    }
}