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
import ru.scisolutions.scicmscore.engine.model.DatasourceType
import ru.scisolutions.scicmscore.engine.persistence.entity.Datasource
import ru.scisolutions.scicmscore.engine.persistence.service.DatasourceService
import ru.scisolutions.scicmscore.extension.isUUID
import java.sql.DatabaseMetaData
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
    private class DataSourceBucket(
        val dataSource: DataSource,
        val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
        val databaseMetaData: DatabaseMetaData
    )

    private val buckets: Cache<String, DataSourceBucket> =
        CacheBuilder.newBuilder()
            .expireAfterAccess(dataProps.datasourceCacheExpirationMinutes, TimeUnit.MINUTES)
            .removalListener<String, DataSourceBucket> {
                RemovalListener<String, DataSourceBucket> {
                    val dataSource = it.value?.dataSource
                    if (it.key != Datasource.MAIN_DATASOURCE_NAME && dataSource is HikariDataSource) {
                        dataSource.close()
                    }
                }
            }
            .build()

    fun dataSource(ds: String): DataSource =
        dataSourceBucket(ds).dataSource

    private fun dataSourceBucket(ds: String): DataSourceBucket {
        val name = if (ds.isUUID()) datasourceService.getById(ds).name else ds

        return buckets.get(name) {
            if (name == Datasource.MAIN_DATASOURCE_NAME) createDataSourceBucket(mainDataSource) else createDataSourceBucket(name)
        }
    }

    private fun createDataSourceBucket(dataSource: DataSource): DataSourceBucket =
        DataSourceBucket(
            dataSource = dataSource,
            namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource),
            databaseMetaData = dataSource.connection.use { it.metaData }
        )

    private fun createDataSourceBucket(name: String): DataSourceBucket {
        val datasource = datasourceService.getByName(name)
        val sourceType = datasource.sourceType
        if (sourceType == DatasourceType.SPREADSHEET || sourceType == DatasourceType.CSV) {
            throw IllegalArgumentException("Datasource [$name] has invalid type.")
        }

        val config = HikariConfig().apply {
            this.jdbcUrl = environment.resolvePlaceholders(requireNotNull(datasource.connectionString))
            this.username = environment.resolvePlaceholders(requireNotNull(datasource.username))
            this.password = environment.resolvePlaceholders(requireNotNull(datasource.password))
            this.maximumPoolSize = datasource.maxPoolSize ?: dataProps.defaultPoolSize
            this.minimumIdle = datasource.minIdle ?: dataProps.defaultIdle
        }

        return createDataSourceBucket(HikariDataSource(config))
    }

    fun template(ds: String): NamedParameterJdbcTemplate =
        dataSourceBucket(ds).namedParameterJdbcTemplate

    fun databaseMetaData(ds: String): DatabaseMetaData =
        dataSourceBucket(ds).databaseMetaData

    fun checkConnection(url: String, user: String, password: String) {
        (
            DriverManager.getConnection(
                environment.resolvePlaceholders(url),
                environment.resolvePlaceholders(user),
                environment.resolvePlaceholders(password)
            )
            ).use {}
    }
}
