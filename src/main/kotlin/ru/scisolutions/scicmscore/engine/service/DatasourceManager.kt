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
import ru.scisolutions.scicmscore.persistence.entity.Datasource
import ru.scisolutions.scicmscore.persistence.service.DatasourceService
import java.sql.DriverManager
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
        .expireAfterAccess(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .removalListener<String, DataSource> {
            RemovalListener<String, DataSource> {
                val datasource = it.value
                if (it.key != Datasource.MAIN_DATASOURCE_NAME && datasource is HikariDataSource)
                    datasource.close()
            }
        }
        .build()

    fun dataSource(name: String): DataSource =
        dataSourceCache.get(name) {
            if (name == Datasource.MAIN_DATASOURCE_NAME) mainDataSource else createDataSource(name)
        }

    private fun createDataSource(name: String): DataSource {
        val ds = datasourceService.getByName(name)
        val config = HikariConfig().apply {
            this.jdbcUrl = environment.resolvePlaceholders(ds.connectionString)
            this.username = environment.resolvePlaceholders(ds.username)
            this.password = environment.resolvePlaceholders(ds.password)
            this.maximumPoolSize = ds.maxPoolSize ?: dataProps.defaultPoolSize
            this.minimumIdle = ds.minIdle ?: dataProps.defaultIdle
        }

        return HikariDataSource(config)
    }

    fun template(name: String): NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(dataSource(name))

    fun checkConnection(url: String, user: String, password: String) {
        (DriverManager.getConnection(
            environment.resolvePlaceholders(url),
            environment.resolvePlaceholders(user),
            environment.resolvePlaceholders(password)
        )).use {}
    }
}