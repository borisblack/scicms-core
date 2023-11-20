package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.JdbcEscape
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.util.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.util.UUID

object SQL {
    fun toSqlValue(value: Any?) =
        when (value) {
            is UUID -> value.toString()
            is LocalDate -> JdbcEscape.date(value)
            is LocalTime -> JdbcEscape.timestamp(value.atDate(LocalDate.EPOCH)) // JdbcEscape.time not works on Oracle DATE type
            is OffsetTime -> JdbcEscape.timestamp(value.atDate(LocalDate.EPOCH)) // JdbcEscape.time not works on Oracle DATE type
            is LocalDateTime -> JdbcEscape.timestamp(value)
            is OffsetDateTime -> JdbcEscape.timestamp(value)
            else -> value
        }

    fun toSqlValue(value: Any?, type: FieldType) =
        when (type) {
            FieldType.uuid,
            FieldType.string,
            FieldType.enum,
            FieldType.sequence,
            FieldType.email,
            FieldType.password,
            FieldType.media,
            FieldType.relation,
            FieldType.text,
            FieldType.bool,
            FieldType.int,
            FieldType.long,
            FieldType.float,
            FieldType.double,
            FieldType.decimal,
            FieldType.date -> value
            FieldType.time -> if (value is OffsetTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value
            FieldType.datetime,
            FieldType.timestamp -> if (value is OffsetDateTime) value.withOffsetSameLocal(ZoneOffset.UTC) else value
            FieldType.array,
            FieldType.json -> if (value is String) value else Json.objectMapper.writeValueAsString(value)
        }

    @Deprecated("Incorrect parsing in case of numerical strings with non-numerical meaning")
    fun toSqlValueWithParsing(value: Any?) =
        if (value is String) parseStringValue(value) else toSqlValue(value)

    @Deprecated("Incorrect parsing in case of numerical strings with non-numerical meaning")
    private fun parseStringValue(value: String): Any =
        if (Numeric.isInt(value)) {
            value.toInt()
        } else if (Numeric.isLong(value)) {
            value.toLong()
        } else if (Numeric.isFloat(value)) {
            value.toFloat()
        } else if (Numeric.isDouble(value)) {
            value.toDouble()
        } else if (value == "true" || value == "false") {
            value.toBoolean()
        } else if (DateTime.isDate(value)) {
            JdbcEscape.date(DateTime.parseDate(value))
        } else if (DateTime.isTime(value)) {
            JdbcEscape.timestamp(DateTime.parseTime(value).atDate(LocalDate.EPOCH)) // JdbcEscape.time not works on Oracle DATE type
        } else if (DateTime.isDateTime(value)) {
            JdbcEscape.timestamp(DateTime.parseDateTime(value))
        } else value
}