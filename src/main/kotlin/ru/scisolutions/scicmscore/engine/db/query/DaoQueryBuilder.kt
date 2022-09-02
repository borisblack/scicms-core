package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.ComboCondition.Op
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.DeleteQuery
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.InsertQuery
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.UpdateQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

class DaoQueryBuilder {
    fun buildFindByIdQuery(item: Item, id: String, selectAttrNames: Set<String>? = null, permissionIds: Set<String>? = null): SelectQuery {
        val table = createTable(item)
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

    private fun createTable(item: Item): DbTable {
        val spec = DbSpec()
        val schema = spec.addDefaultSchema()
        return DbTable(schema, item.tableName)
    }

    fun buildFindAllByAttributeQuery(item: Item, attrName: String, attrValue: Any, permissionIds: Set<String>? = null): SelectQuery {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        val table = createTable(item)
        val attrCol = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
        val query = SelectQuery()
            .addAllColumns()
            .addCondition(BinaryCondition.equalTo(attrCol, SQL.toSqlValue(attrValue)))

        val permissionCondition = getPermissionCondition(table, permissionIds)
        if (permissionCondition != null)
            query.addCondition(permissionCondition)

        return query.validate()
    }

    fun buildFindByIdsQuery(item: Item, ids: Set<String>, permissionIds: Set<String>? = null): SelectQuery {
        if (ids.isEmpty())
            throw IllegalArgumentException("ID set is empty")

        val table = createTable(item)
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
        val table = createTable(item)
        val query = InsertQuery(table)
        itemRec.forEach { (attrName, value) ->
            val attribute = item.spec.getAttributeOrThrow(attrName)
            val column = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
            query.addColumn(column, SQL.toSqlValue(value))
        }

        return query.validate()
    }

    fun buildUpdateByAttributeQuery(item: Item, attrName: String, attrValue: Any?, itemRec: ItemRec, permissionIds: Set<String>? = null): UpdateQuery =
        buildUpdateByAttributesQuery(item, mapOf(attrName to attrValue), itemRec, permissionIds)

    fun buildUpdateByAttributesQuery(item: Item, attributes: Map<String, Any?>, itemRec: ItemRec, permissionIds: Set<String>? = null): UpdateQuery {
        val table = createTable(item)
        val conditions = attributes.map { (attrName, value) ->
            val attribute = item.spec.getAttributeOrThrow(attrName)
            val attrCol = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
            if (value == null)
                UnaryCondition.isNull(attrCol)
            else
                BinaryCondition.equalTo(attrCol, SQL.toSqlValue(value))
        }

        val query = UpdateQuery(table)
            .addCondition(ComboCondition(Op.AND, *conditions.toTypedArray()))

        itemRec.forEach { (recAttrName, recValue) ->
            val recAttribute = item.spec.getAttributeOrThrow(recAttrName)
            val column = DbColumn(table, recAttribute.columnName ?: recAttrName.lowercase(), null, null)
            query.addSetClause(column, SQL.toSqlValue(recValue))
        }

        val permissionCondition = getPermissionCondition(table, permissionIds)
        if (permissionCondition != null)
            query.addCondition(permissionCondition)

        return query.validate()
    }

    fun buildDeleteByAttributeQuery(item: Item, attrName: String, attrValue: Any, permissionIds: Set<String>? = null): DeleteQuery {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        val table = createTable(item)
        val attrCol = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
        val query = DeleteQuery(table)
            .addCondition(BinaryCondition.equalTo(attrCol, SQL.toSqlValue(attrValue)))

        val permissionCondition = getPermissionCondition(table, permissionIds)
        if (permissionCondition != null)
            query.addCondition(permissionCondition)

        return query.validate()
    }

    fun buildLockByAttributeQuery(item: Item, attrName: String, attrValue: Any, userId: String): UpdateQuery {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        val table = createTable(item)
        val attrCol = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
        val lockedByIddCol = DbColumn(table, LOCKED_BY_ID_COL_NAME, null, null)
        val query = UpdateQuery(table)
            .addSetClause(lockedByIddCol, userId)
            .addCondition(BinaryCondition.equalTo(attrCol, SQL.toSqlValue(attrValue)))
            .addCondition(ComboCondition(Op.OR, UnaryCondition.isNull(lockedByIddCol), BinaryCondition.equalTo(lockedByIddCol, userId)))

        return query.validate()
    }

    fun buildUnlockByAttributeQuery(item: Item, attrName: String, attrValue: Any, userId: String): UpdateQuery {
        val attribute = item.spec.getAttributeOrThrow(attrName)
        val table = createTable(item)
        val attrCol = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
        val lockedByIddCol = DbColumn(table, LOCKED_BY_ID_COL_NAME, null, null)
        val query = UpdateQuery(table)
            .addSetClause(lockedByIddCol, null)
            .addCondition(BinaryCondition.equalTo(attrCol, SQL.toSqlValue(attrValue)))
            .addCondition(ComboCondition(Op.OR, UnaryCondition.isNull(lockedByIddCol), BinaryCondition.equalTo(lockedByIddCol, userId)))

        return query.validate()
    }

    companion object {
        private const val ID_COL_NAME = "id"
        private const val LOCKED_BY_ID_COL_NAME = "locked_by_id"
        private const val PERMISSION_ID_COL_NAME = "permission_id"
    }
}