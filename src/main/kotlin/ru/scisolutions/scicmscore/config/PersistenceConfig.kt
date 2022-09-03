package ru.scisolutions.scicmscore.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.EnableTransactionManagement
import ru.scisolutions.scicmscore.config.props.DataProps
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class PersistenceConfig(
    private val dataProps: DataProps
) {
    @Bean
    fun dataSourceMap(): DataSourceMap {
        val map = dataProps.dataSources.mapValues { (_, config) -> HikariDataSource(config) }

        return DataSourceMap(map)
    }

    @Bean
    fun dataSource(): DataSource = dataSourceMap().main

    @Bean
    fun jdbcTemplateMap(): JdbcTemplateMap {
        val map = dataSourceMap().mapValues { (_, ds) -> NamedParameterJdbcTemplate(ds) }

        return JdbcTemplateMap(map)
    }

    @Bean
    fun jdbcTemplate(): JdbcTemplate = JdbcTemplate(dataSource())

    class DataSourceMap(map: Map<String, DataSource>) : Map<String, DataSource> by map {
        val main: DataSource = getOrThrow(MAIN_KEY)

        fun getOrThrow(key: String) = this[key] ?: throw IllegalArgumentException("Datasource [$key] not found")
    }

    class JdbcTemplateMap(map: Map<String, NamedParameterJdbcTemplate>) : Map<String, NamedParameterJdbcTemplate> by map {
        val main: NamedParameterJdbcTemplate = getOrThrow(MAIN_KEY)

        fun getOrThrow(key: String) = this[key] ?: throw IllegalArgumentException("JdbcTemplate [$key] not found")
    }

    companion object {
        const val MAIN_KEY = "main"
    }
}