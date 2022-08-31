package ru.scisolutions.scicmscore.engine.db.query

import com.healthmarketscience.sqlbuilder.JdbcEscape
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.util.UUID

object SQL {
    fun toSqlValue(value: Any?) =
        when (value) {
            is UUID -> value.toString()
            is LocalDate -> JdbcEscape.date(value)
            is LocalTime -> JdbcEscape.time(value)
            is OffsetTime -> JdbcEscape.time(value)
            is LocalDateTime -> JdbcEscape.timestamp(value)
            is OffsetDateTime -> JdbcEscape.timestamp(value)
            else -> value
        }
}