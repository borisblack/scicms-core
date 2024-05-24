package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.BinaryCondition
import com.healthmarketscience.sqlbuilder.OrderObject.Dir
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.Attribute.RelType
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import java.util.regex.Pattern

@Component
class ItemOrderingsParser(private val itemService: ItemService) {
    fun parseOrderings(item: Item, inputSortList: List<String>, schema: DbSchema, table: DbTable, query: SelectQuery) =
        inputSortList.forEach { parseOrdering(item, it, schema, query, table) }

    private fun parseOrdering(item: Item, inputSort: String, schema: DbSchema, query: SelectQuery, table: DbTable) {
        val matcher = sortAttrPattern.matcher(inputSort)
        if (!matcher.matches())
            throw IllegalArgumentException("Invalid sort expression: $inputSort")

        val attrName = matcher.group(1)
        val attribute = item.spec.getAttribute(attrName)
        val col = DbColumn(table, attribute.columnName ?: attrName.lowercase(), null, null)
        val nestedAttrName = matcher.group(2)
        val order = matcher.group(3) ?: "asc"
        val orderDir = if (order == "desc") Dir.DESCENDING else Dir.ASCENDING
        if (nestedAttrName == null) {
            query.addOrdering(col, orderDir)
        } else {
            when (attribute.type) {
                FieldType.relation -> {
                    if (attribute.relType == RelType.oneToMany || attribute.relType == RelType.manyToMany)
                        throw IllegalArgumentException("Invalid sort attribute")

                    val target = itemService.getByName(requireNotNull(attribute.target))
                    addOrdering(target, nestedAttrName, schema, query, table, col, orderDir)
                }
                FieldType.media -> addOrdering(itemService.getMedia(), nestedAttrName, schema, query, table, col, orderDir)
                else -> query.addOrdering(col, orderDir)
            }
        }
    }

    private fun addOrdering(target: Item, targetAttrName: String, schema: DbSchema, query: SelectQuery, table: DbTable, col: DbColumn, orderDir: Dir) {
        val targetTable = schema.addTable(requireNotNull(target.tableName))
        val targetOrderingAttribute = target.spec.getAttribute(targetAttrName)
        val targetOrderingCol = DbColumn(targetTable, targetOrderingAttribute.columnName ?: targetAttrName.lowercase(), null, null)
        val targetIdCol = DbColumn(targetTable, target.idColName, null, null)
        query.addJoin(SelectQuery.JoinType.LEFT_OUTER, table, targetTable, BinaryCondition.equalTo(col, targetIdCol))
        query.addOrdering(targetOrderingCol, orderDir)
    }

    companion object {
        private val sortAttrPattern = Pattern.compile("^(\\w+)\\.?(\\w+)?(?::(asc|desc))?\$", Pattern.CASE_INSENSITIVE)
    }
}