package ru.scisolutions.scicmscore.engine.db.query

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTime {
    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val timeFormatter = DateTimeFormatter.ISO_TIME
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    fun isDate(source: String): Boolean =
        try {
            dateFormatter.parse(source)
            true
        } catch (e: DateTimeParseException) {
            false
        }

    fun isTime(source: String): Boolean =
        try {
            timeFormatter.parse(source)
            true
        } catch (e: DateTimeParseException) {
            false
        }

    fun isDateTime(source: String): Boolean =
        try {
            dateTimeFormatter.parse(source)
            true
        } catch (e: DateTimeParseException) {
            false
        }

    fun parseDate(source: String): LocalDate = LocalDate.parse(source, dateFormatter)

    fun parseTime(source: String): LocalTime = LocalTime.parse(source, timeFormatter)

    fun parseDateTime(source: String): LocalDateTime = LocalDateTime.parse(source, dateTimeFormatter)
}