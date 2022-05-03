package ru.scisolutions.scicmscore.engine.data.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.CustomSql
import com.healthmarketscience.sqlbuilder.InCondition
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.util.AccessUtil

class QueryBuilder {
    fun buildFindByIdQueryForRead(item: Item, id: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryForRead(item, ID_ATTR_NAME, id, selectAttrNames)

    fun buildFindByIdQueryForWrite(item: Item, id: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryForWrite(item, ID_ATTR_NAME, id, selectAttrNames)

    fun buildFindByIdQueryForCreate(item: Item, id: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryForCreate(item, ID_ATTR_NAME, id, selectAttrNames)

    fun buildFindByIdQueryForDelete(item: Item, id: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryForDelete(item, ID_ATTR_NAME, id, selectAttrNames)

    fun buildFindByIdQueryForAdministration(item: Item, id: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryForAdministration(item, ID_ATTR_NAME, id, selectAttrNames)

    fun buildFindByIdQueryFor(item: Item, id: String, selectAttrNames: Set<String>, accessMask: Set<Int>) =
        buildFindByKeyAttrNameQueryFor(item, ID_ATTR_NAME, id, selectAttrNames, accessMask)

    fun buildFindByKeyAttrNameQueryForRead(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.readMask)

    fun buildFindByKeyAttrNameQueryForWrite(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.writeMask)

    fun buildFindByKeyAttrNameQueryForCreate(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.createMask)

    fun buildFindByKeyAttrNameQueryForDelete(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.deleteMask)

    fun buildFindByKeyAttrNameQueryForAdministration(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>) =
        buildFindByKeyAttrNameQueryFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.administrationMask)

    fun buildFindByKeyAttrNameQueryFor(
        item: Item,
        keyAttrName: String,
        keyAttrValue: String,
        selectAttrNames: Set<String>,
        accessMask: Set<Int>
    ): SelectQuery {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = DbTable(schema, item.tableName)

        val keyAttribute = item.spec.getAttributeOrThrow(keyAttrName)
        val keyColName = keyAttribute.columnName ?: keyAttrName.lowercase()
        val keyCol = DbColumn(table, keyColName, null, null)
        val permissionIdCol = DbColumn(table, PERMISSION_ID_COL_NAME, null, null)
        val columns = selectAttrNames
            .map {
                val attribute = item.spec.getAttributeOrThrow(it)
                DbColumn(table, attribute.columnName ?: it.lowercase(), null, null)
            }
            .toTypedArray()

        return SelectQuery()
            .addColumns(*columns)
            .addCondition(BinaryCondition.equalTo(keyCol, keyAttrValue))
            .addCondition(
                InCondition(
                    permissionIdCol,
                    CustomSql(AccessUtil.getPermissionIdsStatement(accessMask))
                )
            )
            .validate()
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private const val PERMISSION_ID_COL_NAME = "permission_id"
    }
}