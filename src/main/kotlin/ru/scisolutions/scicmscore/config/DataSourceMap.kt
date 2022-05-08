package ru.scisolutions.scicmscore.config

import javax.sql.DataSource

class DataSourceMap(map: Map<String, DataSource>) : Map<String, DataSource> by map {
    val main: DataSource = getOrThrow(MAIN_KEY)

    fun getOrThrow(key: String) = this[key] ?: throw IllegalArgumentException("Datasource [$key] not found")

    companion object {
        const val MAIN_KEY = "main"
    }
}