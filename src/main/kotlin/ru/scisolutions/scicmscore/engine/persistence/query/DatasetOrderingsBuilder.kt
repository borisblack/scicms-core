package ru.scisolutions.scicmscore.engine.persistence.query

import com.healthmarketscience.sqlbuilder.OrderObject.Dir
import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable
import java.util.regex.Pattern

class DatasetOrderingsBuilder {
    fun addOrderings(inputSortList: List<String>, table: DbTable?, query: SelectQuery) =
        inputSortList.forEach { addOrdering(it, table, query) }

    private fun addOrdering(inputSort: String, table: DbTable?, query: SelectQuery) {
        val matcher = sortAttrPattern.matcher(inputSort)
        if (!matcher.matches()) {
            throw IllegalArgumentException("Invalid sort expression: $inputSort")
        }

        val fieldName = matcher.group(1)
        val order = matcher.group(2) ?: "asc"
        val orderDir = if (order == "desc") Dir.DESCENDING else Dir.ASCENDING
        if (table == null) {
            query.addCustomOrdering(fieldName, orderDir)
        } else {
            val col = DbColumn(table, fieldName, null, null)
            query.addOrdering(col, orderDir)
        }
    }

    companion object {
        private val sortAttrPattern = Pattern.compile("^(\\w+)(?::(asc|desc))?\$", Pattern.CASE_INSENSITIVE)
    }
}
