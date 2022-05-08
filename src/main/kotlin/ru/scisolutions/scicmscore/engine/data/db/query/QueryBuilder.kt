package ru.scisolutions.scicmscore.engine.data.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.UnaryCondition
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import ru.scisolutions.scicmscore.persistence.entity.Item

class QueryBuilder {
    fun buildFindByKeyAttrNameQuery(
        item: Item,
        keyAttrName: String,
        keyAttrValue: String,
        selectAttrNames: Set<String>,
        permissionIds: Set<String>
    ): SelectQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)

        val keyAttribute = item.spec.getAttributeOrThrow(keyAttrName)
        val keyColName = keyAttribute.columnName ?: keyAttrName.lowercase()
        val keyCol = DbColumn(table, keyColName, null, null)
        val permissionIdCol = DbColumn(table, PERMISSION_ID_COL_NAME, null, null)
        val permissionCondition = if (permissionIds.isEmpty()) UnaryCondition.isNull(permissionIdCol) else InCondition(permissionIdCol, *permissionIds.toTypedArray())
        val columns = selectAttrNames
            .map {
                val attribute = item.spec.getAttributeOrThrow(it)
                DbColumn(table, attribute.columnName ?: it.lowercase(), null, null)
            }
            .toTypedArray()

        return SelectQuery()
            .addColumns(*columns)
            .addCondition(BinaryCondition.equalTo(keyCol, keyAttrValue))
            .addCondition(permissionCondition)
            .validate()
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private const val PERMISSION_ID_COL_NAME = "permission_id"
    }
}