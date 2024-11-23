package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.ComboCondition
import com.healthmarketscience.sqlbuilder.ComboCondition.Op
import com.healthmarketscience.sqlbuilder.Condition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.DeleteQuery
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.InsertQuery
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.UpdateQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item

class ItemQueryBuilder {
    fun buildFindByIdQuery(
        item: Item,
        id: String,
        paramSource: AttributeSqlParameterSource,
        selectAttrNames: Set<String>? = null,
        permissionIds: Set<String>? = null
    ): SelectQuery = buildFindByKeyQuery(
        item = item,
        keyAttrName = item.idAttribute,
        key = id,
        paramSource = paramSource,
        selectAttrNames = selectAttrNames,
        permissionIds = permissionIds
    )

    fun buildFindByKeyQuery(
        item: Item,
        keyAttrName: String,
        key: String,
        paramSource: AttributeSqlParameterSource,
        selectAttrNames: Set<String>? = null,
        permissionIds: Set<String>? = null
    ): SelectQuery {
        val table = createTable(item)
        val keyAttribute = item.spec.getAttribute(keyAttrName)
        val keyColName = keyAttribute.getColumnName(keyAttrName)
        val keyCol = DbColumn(table, keyColName, null, null)
        val query = SelectQuery()

        if (selectAttrNames == null) {
            query.addAllColumns()
        } else {
            val columns =
                selectAttrNames
                    .map {
                        val attribute = item.spec.getAttribute(it)
                        DbColumn(table, attribute.getColumnName(it), null, null)
                    }
                    .toTypedArray()

            query.addColumns(*columns)
        }

        val sqlParamName = "${table.alias}_$keyColName"
        query.addCondition(BinaryCondition.equalTo(keyCol, CustomSql(":$sqlParamName")))
        paramSource.addValue(sqlParamName, key, keyAttribute.type)

        if (permissionIds != null) {
            val permissionCondition = getPermissionCondition(table, permissionIds)
            query.addCondition(permissionCondition)
        }

        return query.validate()
    }

    private fun createTable(item: Item): DbTable {
        val spec = DbSpec()
        val schema = spec.addDefaultSchema()
        return DbTable(schema, item.qs)
    }

    fun buildFindAllByAttributeQuery(
        item: Item,
        attrName: String,
        attrValue: Any,
        paramSource: AttributeSqlParameterSource,
        permissionIds: Set<String>? = null
    ): SelectQuery {
        val attribute = item.spec.getAttribute(attrName)
        val table = createTable(item)
        val colName = attribute.columnName ?: attrName.lowercase()
        val attrCol = DbColumn(table, colName, null, null)
        val sqlParamName = "${table.alias}_$colName"
        val query =
            SelectQuery()
                .addAllColumns()
                .addCondition(BinaryCondition.equalTo(attrCol, CustomSql(":$sqlParamName")))

        paramSource.addValue(sqlParamName, attrValue, attribute.type)

        if (permissionIds != null) {
            val permissionCondition = getPermissionCondition(table, permissionIds)
            query.addCondition(permissionCondition)
        }

        return query.validate()
    }

    fun buildFindAllByIdsQuery(
        item: Item,
        ids: Set<String>,
        paramSource: AttributeSqlParameterSource,
        permissionIds: Set<String>? = null
    ): SelectQuery = buildFindAllByKeysQuery(
        item = item,
        keyAttrName = item.idAttribute,
        keys = ids,
        paramSource = paramSource,
        permissionIds = permissionIds
    )

    fun buildFindAllByKeysQuery(
        item: Item,
        keyAttrName: String,
        keys: Set<String>,
        paramSource: AttributeSqlParameterSource,
        permissionIds: Set<String>? = null
    ): SelectQuery {
        if (keys.isEmpty()) {
            throw IllegalArgumentException("Keys set is empty")
        }

        val table = createTable(item)
        val keyAttribute = item.spec.getAttribute(keyAttrName)
        val keyCol = DbColumn(table, keyAttribute.getColumnName(keyAttrName), null, null)
        val query =
            SelectQuery()
                .addAllColumns()
                .addCondition(InCondition(keyCol, *keys.toTypedArray()))

        if (permissionIds != null) {
            val permissionCondition = getPermissionCondition(table, permissionIds)
            query.addCondition(permissionCondition)
        }

        return query.validate()
    }

    private fun getPermissionCondition(table: DbTable, permissionIds: Set<String>): Condition {
        val permissionIdCol = DbColumn(table, ItemRec.PERMISSION_COL_NAME, null, null)

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

    fun buildInsertQuery(item: Item, itemRec: ItemRec, paramSource: AttributeSqlParameterSource): InsertQuery {
        val table = createTable(item)
        val query = InsertQuery(table)
        itemRec.forEach { (attrName, value) ->
            val attribute = item.spec.getAttribute(attrName)
            val colName = attribute.columnName ?: attrName.lowercase()
            val column = DbColumn(table, colName, null, null)
            val sqlParamName = "${table.alias}_$colName"
            query.addColumn(column, CustomSql(":$sqlParamName"))
            paramSource.addValue(sqlParamName, value, attribute.type)
            // query.addColumn(column, SQL.toSqlValue(value))
        }

        return query.validate()
    }

    fun buildUpdateByAttributeQuery(
        item: Item,
        whereAttrName: String,
        whereAttrValue: Any?,
        updateAttributes: Map<String, Any?>,
        paramSource: AttributeSqlParameterSource,
        permissionIds: Set<String>? = null
    ): UpdateQuery =
        buildUpdateByAttributesQuery(item, mapOf(whereAttrName to whereAttrValue), updateAttributes, paramSource, permissionIds)

    fun buildUpdateByAttributesQuery(
        item: Item,
        whereAttributes: Map<String, Any?>,
        updateAttributes: Map<String, Any?>,
        paramSource: AttributeSqlParameterSource,
        permissionIds: Set<String>? = null
    ): UpdateQuery {
        val table = createTable(item)
        val conditions =
            whereAttributes.map { (attrName, value) ->
                val attribute = item.spec.getAttribute(attrName)
                val colName = attribute.columnName ?: attrName.lowercase()
                val attrCol = DbColumn(table, colName, null, null)
                if (value == null) {
                    UnaryCondition.isNull(attrCol)
                } else {
                    val sqlParamName = "${table.alias}_$colName"
                    paramSource.addValue(sqlParamName, value, attribute.type)
                    BinaryCondition.equalTo(attrCol, CustomSql(":$sqlParamName"))
                    // BinaryCondition.equalTo(attrCol, SQL.toSqlValue(value))
                }
            }

        val query =
            UpdateQuery(table)
                .addCondition(ComboCondition(Op.AND, *conditions.toTypedArray()))

        updateAttributes.forEach { (recAttrName, recValue) ->
            val recAttribute = item.spec.getAttribute(recAttrName)
            val recColName = recAttribute.columnName ?: recAttrName.lowercase()
            val column = DbColumn(table, recColName, null, null)
            val sqlParamName = "${table.alias}_${recColName}_new"
            query.addSetClause(column, CustomSql(":$sqlParamName"))
            paramSource.addValue(sqlParamName, recValue, recAttribute.type)
            // query.addSetClause(column, SQL.toSqlValue(recValue))
        }

        if (permissionIds != null) {
            val permissionCondition = getPermissionCondition(table, permissionIds)
            query.addCondition(permissionCondition)
        }

        return query.validate()
    }

    fun buildDeleteByAttributeQuery(
        item: Item,
        attrName: String,
        attrValue: Any,
        paramSource: AttributeSqlParameterSource,
        permissionIds: Set<String>? = null
    ): DeleteQuery {
        val attribute = item.spec.getAttribute(attrName)
        val table = createTable(item)
        val colName = attribute.columnName ?: attrName.lowercase()
        val attrCol = DbColumn(table, colName, null, null)
        val sqlParamName = "${table.alias}_$colName"
        val query =
            DeleteQuery(table)
                .addCondition(BinaryCondition.equalTo(attrCol, CustomSql(":$sqlParamName")))

        paramSource.addValue(sqlParamName, attrValue, attribute.type)

        if (permissionIds != null) {
            val permissionCondition = getPermissionCondition(table, permissionIds)
            query.addCondition(permissionCondition)
        }

        return query.validate()
    }

    fun buildLockByAttributeQuery(
        item: Item,
        attrName: String,
        attrValue: Any,
        userId: String,
        paramSource: AttributeSqlParameterSource
    ): UpdateQuery {
        val attribute = item.spec.getAttribute(attrName)
        val table = createTable(item)
        val colName = attribute.columnName ?: attrName.lowercase()
        val attrCol = DbColumn(table, colName, null, null)
        val lockedByIddCol = DbColumn(table, ItemRec.LOCKED_BY_COL_NAME, null, null)
        val sqlParamName = "${table.alias}_$colName"
        val query =
            UpdateQuery(table)
                .addSetClause(lockedByIddCol, userId)
                .addCondition(BinaryCondition.equalTo(attrCol, CustomSql(":$sqlParamName")))
                .addCondition(ComboCondition(Op.OR, UnaryCondition.isNull(lockedByIddCol), BinaryCondition.equalTo(lockedByIddCol, userId)))

        paramSource.addValue(sqlParamName, attrValue, attribute.type)

        return query.validate()
    }

    fun buildUnlockByAttributeQuery(
        item: Item,
        attrName: String,
        attrValue: Any,
        userId: String,
        paramSource: AttributeSqlParameterSource
    ): UpdateQuery {
        val attribute = item.spec.getAttribute(attrName)
        val table = createTable(item)
        val colName = attribute.columnName ?: attrName.lowercase()
        val attrCol = DbColumn(table, colName, null, null)
        val lockedByIddCol = DbColumn(table, ItemRec.LOCKED_BY_COL_NAME, null, null)
        val sqlParamName = "${table.alias}_$colName"
        val query =
            UpdateQuery(table)
                .addSetClause(lockedByIddCol, null)
                .addCondition(BinaryCondition.equalTo(attrCol, CustomSql(":$sqlParamName")))
                .addCondition(ComboCondition(Op.OR, UnaryCondition.isNull(lockedByIddCol), BinaryCondition.equalTo(lockedByIddCol, userId)))

        paramSource.addValue(sqlParamName, attrValue, attribute.type)

        return query.validate()
    }
}
