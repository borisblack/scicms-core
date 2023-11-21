package ru.scisolutions.scicmscore.engine.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalListener
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.service.DatasourceService
import ru.scisolutions.scicmscore.util.Schema
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

@Service
class DatasourceManager(
    private val environment: Environment,
    private val dataProps: DataProps,
    private val mainDataSource: DataSource,
    private val datasourceService: DatasourceService
) {
    private val dataSourceCache: Cache<String, DataSource> = CacheBuilder.newBuilder()
        .expireAfterAccess(dataProps.datasourceCacheExpirationMinutes, TimeUnit.MINUTES)
        .removalListener<String, DataSource> {
            RemovalListener<String, DataSource> {
                val datasource = it.value
                if (it.key != Schema.MAIN_DATA_SOURCE_NAME && datasource is HikariDataSource)
                    datasource.close()
            }
        }
        .build()

    fun dataSource(name: String?): DataSource =
        dataSourceCache.get(name ?: Schema.MAIN_DATA_SOURCE_NAME) {
            if (name == null || name == Schema.MAIN_DATA_SOURCE_NAME) {
                mainDataSource
            } else {
                val ds = datasourceService.getByName(name)
                val config = HikariConfig().apply {
                    this.jdbcUrl = environment.resolvePlaceholders(ds.connectionString)
                    this.username = environment.resolvePlaceholders(ds.username)
                    this.password = environment.resolvePlaceholders(ds.password)
                    this.maximumPoolSize = ds.maxPoolSize ?: dataProps.defaultPoolSize
                    this.minimumIdle = ds.minIdle ?: dataProps.defaultIdle
                }

                HikariDataSource(config)
            }
        }

    fun template(name: String?): NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(dataSource(name ?: Schema.MAIN_DATA_SOURCE_NAME))
}