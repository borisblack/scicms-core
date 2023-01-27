package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.OrderObject.Dir
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import java.util.regex.Pattern

class DatasetOrderingsParser {
    fun parseOrderings(inputSortList: List<String>, schema: DbSchema, table: DbTable, query: SelectQuery) =
        inputSortList.forEach { parseOrdering(it, schema, table, query) }

    private fun parseOrdering(inputSort: String, schema: DbSchema, table: DbTable, query: SelectQuery) {
        val matcher = sortAttrPattern.matcher(inputSort)
        if (!matcher.matches())
            throw IllegalArgumentException("Invalid sort expression: $inputSort")

        val fieldName = matcher.group(1)
        val col = DbColumn(table, fieldName, null, null)
        val order = matcher.group(2) ?: "asc"
        val orderDir = if (order == "desc") Dir.DESCENDING else Dir.ASCENDING
        query.addOrdering(col, orderDir)
    }

    companion object {
        private val sortAttrPattern = Pattern.compile("^(\\w+)(?::(asc|desc))?\$", Pattern.CASE_INSENSITIVE)
    }
}