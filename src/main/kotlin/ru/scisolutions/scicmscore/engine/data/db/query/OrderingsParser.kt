package ru.scisolutions.scicmscore.engine.data.db.query

import ru.scisolutions.scicmscore.persistence.entity.Item
import java.util.regex.Pattern

class OrderingsParser {
    fun parseOrderings(item: Item, inputSortList: List<String>): List<String> =
        inputSortList.map { parseOrdering(item, it) }

    private fun parseOrdering(item: Item, inputSort: String): String {
        val matcher = sortFieldPattern.matcher(inputSort)
        if (!matcher.matches())
            throw IllegalArgumentException("Invalid sort expression: $inputSort")

        val attribute = item.spec.getAttributeOrThrow(matcher.group(1))
        val order = matcher.group(2) ?: "asc"

        return "${attribute.columnName ?: inputSort.lowercase()} $order"
    }

    companion object {
        private val sortFieldPattern = Pattern.compile("^(\\w+)(?::(asc|desc))?$", Pattern.CASE_INSENSITIVE)
    }
}