package ru.scisolutions.scicmscore.engine.persistence.mapper

import org.springframework.jdbc.core.RowMapper
import ru.scisolutions.scicmscore.engine.model.DatasetRec
import java.sql.Clob
import java.sql.ResultSet
import java.sql.Types
import java.time.ZoneOffset

class DatasetRecMapper : RowMapper<DatasetRec> {
    override fun mapRow(rs: ResultSet, rowNum: Int): DatasetRec {
        val datasetRec = DatasetRec()
        val metaData = rs.metaData
        for (i in 1..metaData.columnCount) {
            val type = metaData.getColumnType(i)
            val columnName = metaData.getColumnName(i).lowercase()
            val value: Any? =
                when (type) {
                    Types.DATE -> rs.getDate(i)?.toLocalDate()
                    Types.TIME -> rs.getTime(i)?.toLocalTime()?.atOffset(ZoneOffset.UTC)
                    Types.TIMESTAMP -> rs.getTimestamp(i)?.toLocalDateTime()?.atOffset(ZoneOffset.UTC)
                    Types.CLOB -> parseText(rs.getObject(i))
                    else -> rs.getObject(i)
                }

            datasetRec[columnName] = value
        }

        return datasetRec
    }

    private fun parseText(value: Any?): Any? = if (value is Clob) value.characterStream.readText() else value
}