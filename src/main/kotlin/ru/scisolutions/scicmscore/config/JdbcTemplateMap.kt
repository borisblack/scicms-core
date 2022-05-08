package ru.scisolutions.scicmscore.config

import org.springframework.jdbc.core.JdbcTemplate

class JdbcTemplateMap(map: Map<String, JdbcTemplate>) : Map<String, JdbcTemplate> by map {
    val main: JdbcTemplate = getOrThrow(MAIN_KEY)

    fun getOrThrow(key: String) = this[key] ?: throw IllegalArgumentException("JdbcTemplate [$key] not found")

    companion object {
        const val MAIN_KEY = "main"
    }
}