package ru.scisolutions.scicmscore.engine.persistence.query

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.util.Json
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.util.UUID

class DatasetSqlParameterSource : MapSqlParameterSource {
    constructor() : super()

    constructor(paramName: String, value: Any?) : super(paramName, value)

    constructor(values: Map<String?, *>?) : super(values)

    fun addValue(paramName: String, value: Any?, type: FieldType): DatasetSqlParameterSource {
        when (type) {
            FieldType.uuid,
            FieldType.string,
            FieldType.enum,
            FieldType.sequence,
            FieldType.email,
            FieldType.password,
            FieldType.media,
            FieldType.relation,
            FieldType.text -> this.addValue(paramName, value, Types.VARCHAR)
            FieldType.bool -> this.addValue(paramName, if (value is String) value.toBoolean() else value, Types.SMALLINT)
            FieldType.int -> this.addValue(paramName, if (value is String) value.toInt() else value, Types.INTEGER)
            FieldType.long -> this.addValue(paramName, if (value is String) value.toLong() else value, Types.BIGINT)
            FieldType.float -> addValue(paramName, if (value is String) value.toFloat() else value, Types.FLOAT)
            FieldType.double -> addValue(paramName, if (value is String) value.toDouble() else value, Types.DOUBLE)
            FieldType.decimal -> this.addValue(paramName, if (value is String) value.toBigDecimal() else value, Types.DECIMAL)
            FieldType.date -> this.addValue(paramName, if (value is String) DateTime.parseDate(value) else value, Types.DATE)
            FieldType.time -> this.addValue(
                paramName,
                if (value is String) DateTime.parseTime(value).withOffsetSameLocal(ZoneOffset.UTC)
                else (if (value is OffsetTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value),
                Types.TIME_WITH_TIMEZONE
            )
            FieldType.datetime,
            FieldType.timestamp -> this.addValue(
                paramName,
                if (value is String) DateTime.parseDateTime(value).withOffsetSameLocal(ZoneOffset.UTC)
                else (if (value is OffsetDateTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value),
                Types.TIMESTAMP_WITH_TIMEZONE
            )
            FieldType.array,
            FieldType.json -> this.addValue(paramName, if (value is String) value else Json.objectMapper.writeValueAsString(value))
        }

        return this
    }

    @Deprecated("Incorrect parsing in case of numerical strings with non-numerical meaning")
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

    @Deprecated("Incorrect parsing in case of numerical strings with non-numerical meaning")
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