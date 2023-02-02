package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.OrderObject.Dir
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.model.Attribute.RelType
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import java.util.regex.Pattern
import ru.scisolutions.scicmscore.model.Attribute.Type as AttrType

@Component
class ItemOrderingsParser(private val itemCache: ItemCache) {
    fun parseOrderings(item: Item, inputSortList: List<String>, schema: DbSchema, table: DbTable, query: SelectQuery) =
        inputSortList.forEach { parseOrdering(item, it, schema, query, table) }

    private fun parseOrdering(item: Item, inputSort: String, schema: DbSchema, query: SelectQuery, table: DbTable) {
        val matcher = sortAttrPattern.matcher(inputSort)
        if (!matcher.matches())
            throw IllegalArgumentException("Invalid sort expression: $inputSort")

        val attrName = matcher.group(1)
        val attribute = item.spec.getAttributeOrThrow(attrName)
        val col = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
        val nestedAttrName = matcher.group(2)
        val order = matcher.group(3) ?: "asc"
        val orderDir = if (order == "desc") Dir.DESCENDING else Dir.ASCENDING
        if (nestedAttrName == null) {
            query.addOrdering(col, orderDir)
        } else {
            when (attribute.type) {
                AttrType.relation -> {
                    if (attribute.relType == RelType.oneToMany || attribute.relType == RelType.manyToMany)
                        throw IllegalArgumentException("Invalid sort attribute")

                    val target = itemCache.getOrThrow(requireNotNull(attribute.target))
                    addOrdering(target, nestedAttrName, schema, query, table, col, orderDir)
                }
                AttrType.media -> addOrdering(itemCache.getMedia(), nestedAttrName, schema, query, table, col, orderDir)
                else -> query.addOrdering(col, orderDir)
            }
        }
    }

    private fun addOrdering(target: Item, targetAttrName: String, schema: DbSchema, query: SelectQuery, table: DbTable, col: DbColumn, orderDir: Dir) {
        val targetTable = schema.addTable(requireNotNull(target.tableName))
        val targetOrderingAttribute = target.spec.getAttributeOrThrow(targetAttrName)
        val targetOrderingCol = DbColumn(targetTable, targetOrderingAttribute.columnName ?: targetAttrName.lowercase(), null, null)
        val targetIdCol = DbColumn(targetTable, ItemRec.ID_COL_NAME, null, null)
        query.addJoin(SelectQuery.JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(col, targetIdCol))
        query.addOrdering(targetOrderingCol, orderDir)
    }

    companion object {
        private val sortAttrPattern = Pattern.compile("^(\\w+)\\.?(\\w+)?(?::(asc|desc))?\$", Pattern.CASE_INSENSITIVE)
    }
}