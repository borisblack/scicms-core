package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.jdbc.support.rowset.SqlRowSetMetaData
import ru.scisolutions.scicmscore.model.Column
import ru.scisolutions.scicmscore.model.FieldType
import java.math.BigDecimal
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types

class ColumnsMapper {
    fun map(metaData: SqlRowSetMetaData): Map<String, Column> {
        val columns = mutableMapOf<String, Column>()
        for (i in 1..metaData.columnCount) {
            columns[metaData.getColumnName(i).lowercase()] = Column(
                type = getColumnType(metaData.getColumnType(i))
            )
        }

        return columns.toMap()
    }

    private fun getColumnCLassName(sqlType: Int): String =
        when (sqlType) {
            Types.NUMERIC, Types.DECIMAL -> BigDecimal::class.java.name
            Types.BIT -> Boolean::class.java.name
            Types.TINYINT -> Byte::class.java.name
            Types.SMALLINT -> Short::class.java.name
            Types.INTEGER -> Int::class.java.name
            Types.BIGINT -> Long::class.java.name
            Types.REAL -> Float::class.java.name
            Types.FLOAT, Types.DOUBLE -> Double::class.java.name
            Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> "byte[]"
            Types.DATE -> Date::class.java.name
            Types.TIME -> Time::class.java.name
            Types.TIMESTAMP -> Timestamp::class.java.name
            Types.BLOB -> Blob::class.java.name
            Types.CLOB -> Clob::class.java.name
            else -> String::class.java.name
        }

    private fun getColumnType(sqlType: Int): FieldType =
        when (sqlType) {
            Types.NUMERIC, Types.DECIMAL -> FieldType.decimal
            Types.BIT -> FieldType.bool
            Types.TINYINT, Types.SMALLINT, Types.INTEGER -> FieldType.int
            Types.BIGINT -> FieldType.long
            Types.REAL -> FieldType.float
            Types.FLOAT, Types.DOUBLE -> FieldType.double
            Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> throw IllegalArgumentException("Unsupported type") // TODO: Think how to handle
            Types.DATE -> FieldType.date
            Types.TIME -> FieldType.time
            Types.TIMESTAMP -> FieldType.timestamp
            Types.BLOB ->  throw IllegalArgumentException("Unsupported type") // TODO: Think how to handle
            Types.CLOB -> FieldType.text
            else -> FieldType.string
        }
}