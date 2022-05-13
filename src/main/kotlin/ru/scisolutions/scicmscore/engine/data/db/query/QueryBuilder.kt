package ru.scisolutions.scicmscore.engine.data.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.ComboCondition.Op
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.InsertQuery
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.UpdateQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

class QueryBuilder {
    fun buildFindByIdQuery(item: Item, id: String, selectAttrNames: Set<String>? = null, permissionIds: Set<String>? = null): SelectQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val idCol = DbColumn(table, ID_COL_NAME, null, null)
        val query = SelectQuery()

        if (selectAttrNames == null) {
            query.addAllColumns()
        } else {
            val columns = selectAttrNames
                .map {
                    val attribute = item.spec.getAttributeOrThrow(it)
                    DbColumn(table, attribute.columnName ?: it.lowercase(), null, null)
                }
                .toTypedArray()

            query.addColumns(*columns)
        }

        query.addCondition(BinaryCondition.equalTo(idCol, id))

        val permissionCondition = getPermissionCondition(table, permissionIds)
        if (permissionCondition != null)
            query.addCondition(permissionCondition)

        return query.validate()
    }

    fun buildFindByIdsQuery(item: Item, ids: Set<String>, permissionIds: Set<String>? = null): SelectQuery {
        if (ids.isEmpty())
            throw IllegalArgumentException("ID set is empty")

        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val idCol = DbColumn(table, ID_COL_NAME, null, null)
        val query = SelectQuery()
            .addAllColumns()
            .addCondition(InCondition(idCol, *ids.toTypedArray()))

        val permissionCondition = getPermissionCondition(table, permissionIds)
        if (permissionCondition != null)
            query.addCondition(permissionCondition)

        return query.validate()
    }

    private fun getPermissionCondition(table: DbTable, permissionIds: Set<String>?): Condition? {
        if (permissionIds == null)
            return null

        val permissionIdCol = DbColumn(table, PERMISSION_ID_COL_NAME, null, null)

        return if (permissionIds.isEmpty()) {
            UnaryCondition.isNull(permissionIdCol)
        } else {
            ComboCondition(
                Op.OR,
                UnaryCondition.isNull(permissionIdCol),
                InCondition(permissionIdCol, *permissionIds.toTypedArray())
            )
        }
    }

    fun buildInsertQuery(item: Item, itemRec: ItemRec): InsertQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val query = InsertQuery(table)
        itemRec.forEach { (attrName, value) ->
            val attribute = item.spec.getAttributeOrThrow(attrName)
            val column = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
            query.addColumn(column, SQL.toSqlValue(value))
        }

        return query.validate()
    }

    fun buildUpdateByIdQuery(item: Item, id: String, itemRec: ItemRec): UpdateQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val idCol = DbColumn(table, ID_COL_NAME, null, null)
        val query = UpdateQuery(table)
            .addCondition(BinaryCondition.equalTo(idCol, id))

        itemRec.forEach { (attrName, value) ->
            val attribute = item.spec.getAttributeOrThrow(attrName)
            val column = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
            query.addSetClause(column, SQL.toSqlValue(value))
        }

        return query.validate()
    }

    fun buildLockByIdQuery(item: Item, id: String, userId: String): UpdateQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val idCol = DbColumn(table, ID_COL_NAME, null, null)
        val lockedByIddCol = DbColumn(table, LOCKED_BY_ID_COL_NAME, null, null)
        val query = UpdateQuery(table)
            .addSetClause(lockedByIddCol, userId)
            .addCondition(BinaryCondition.equalTo(idCol, id))
            .addCondition(ComboCondition(Op.OR, UnaryCondition.isNull(lockedByIddCol), BinaryCondition.equalTo(lockedByIddCol, userId)))

        return query.validate()
    }

    fun buildUnlockByIdQuery(item: Item, id: String, userId: String): UpdateQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)
        val idCol = DbColumn(table, ID_COL_NAME, null, null)
        val lockedByIddCol = DbColumn(table, LOCKED_BY_ID_COL_NAME, null, null)
        val query = UpdateQuery(table)
            .addSetClause(lockedByIddCol, null)
            .addCondition(BinaryCondition.equalTo(idCol, id))
            .addCondition(ComboCondition(Op.OR, UnaryCondition.isNull(lockedByIddCol), BinaryCondition.equalTo(lockedByIddCol, userId)))

        return query.validate()
    }

    companion object {
        private const val ID_COL_NAME = "id"
        private const val LOCKED_BY_ID_COL_NAME = "locked_by_id"
        private const val PERMISSION_ID_COL_NAME = "permission_id"
    }
}