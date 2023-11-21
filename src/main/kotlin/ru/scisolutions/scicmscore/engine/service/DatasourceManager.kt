package ru.scisolutions.scicmscore.engine.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalListener
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.service.DatasourceService
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

@Service
class DatasourceManager(
    private val dataProps: DataProps,
    private val mainDataSource: DataSource,
    private val datasourceService: DatasourceService
) {
    private val dataSourceCache: Cache<String, DataSource> = CacheBuilder.newBuilder()
        .expireAfterAccess(dataProps.datasourceCacheExpirationMinutes, TimeUnit.MINUTES)
        .removalListener<String, DataSource> {
            RemovalListener<String, DataSource> {
                val datasource = it.value
                if (it.key != MAIN_DATA_SOURCE && datasource is HikariDataSource)
                    datasource.close()
            }
        }
        .build()

    fun dataSource(name: String): DataSource =
        dataSourceCache.get(name) {
            if (name == MAIN_DATA_SOURCE) {
                mainDataSource
            } else {
                val ds = datasourceService.getByName(name)
                val config = HikariConfig().apply {
                    this.jdbcUrl = ds.connectionString
                    this.username = ds.username
                    this.password = ds.password
                    this.maximumPoolSize = ds.maxPoolSize ?: dataProps.defaultPoolSize
                    this.minimumIdle = ds.minIdle ?: dataProps.defaultIdle
                }

                HikariDataSource(config)
            }
        }

    fun template(name: String): NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(dataSource(name))

    companion object {
        const val MAIN_DATA_SOURCE = "main"
    }
}