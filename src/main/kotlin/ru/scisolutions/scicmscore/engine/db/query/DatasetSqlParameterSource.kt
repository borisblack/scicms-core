package ru.scisolutions.scicmscore.engine.db.query

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.util.UUID

class DatasetSqlParameterSource : MapSqlParameterSource {
    constructor() : super()

    constructor(paramName: String, value: Any?) : super(paramName, value)

    constructor(values: Map<String?, *>?) : super(values)

    fun addValueWithParsing(paramName: String, value: Any?): DatasetSqlParameterSource {
        when (value) {
            is String -> this.addStringValue(paramName, value)
            is UUID -> this.addValue(paramName, value.toString(), Types.VARCHAR)
            is LocalDate -> this.addValue(paramName, value, Types.DATE)
            is LocalTime -> this.addValue(paramName, value, Types.TIME)
            is LocalDateTime -> this.addValue(paramName, value, Types.TIMESTAMP)
            is OffsetTime -> this.addValue(paramName, value.toLocalTime(), Types.TIME)
            is OffsetDateTime -> this.addValue(paramName, value.toLocalDateTime(), Types.TIMESTAMP)
            else -> this.addValue(paramName, value)
        }

        return this
    }

    private fun addStringValue(paramName: String, value: String): Any =
        if (Numeric.isInt(value)) {
            this.addValue(paramName, value.toInt(), Types.INTEGER)
        } else if (Numeric.isLong(value)) {
            this.addValue(paramName, value.toLong(), Types.BIGINT)
        } else if (Numeric.isFloat(value)) {
            this.addValue(paramName, value.toFloat(), Types.FLOAT)
        } else if (Numeric.isDouble(value)) {
            this.addValue(paramName, value.toDouble(), Types.DOUBLE)
        } else if (value == "true" || value == "false") {
            this.addValue(paramName, value.toBoolean(), Types.BOOLEAN)
        } else if (DateTime.isDate(value)) {
            this.addValue(paramName, DateTime.parseDate(value), Types.DATE)
        } else if (DateTime.isTime(value)) {
            this.addValue(paramName, DateTime.parseTime(value), Types.TIME)
        } else if (DateTime.isDateTime(value)) {
            this.addValue(paramName, DateTime.parseDateTime(value), Types.TIMESTAMP)
        } else this.addValue(paramName, value, Types.VARCHAR)
}