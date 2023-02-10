package ru.scisolutions.scicmscore.engine.db.mapper

import org.springframework.jdbc.core.RowMapper
import java.sql.Clob
import java.sql.ResultSet

class DatasetRowMapper : RowMapper<Map<String, Any?>> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Map<String, Any?> {
        val row = mutableMapOf<String, Any?>()
        val metaData = rs.metaData
        for (i in 1..metaData.columnCount) {
            val columnName = metaData.getColumnName(i).lowercase()
            val value = parseValue(rs.getObject(i))

            row[columnName] = value
        }

        return row
    }

    private fun parseValue(value: Any?): Any? = if (value is Clob) value.characterStream.readText() else value
}