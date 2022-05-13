package ru.scisolutions.scicmscore.config.props

import com.zaxxer.hikari.HikariConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.data")
class DataProps {
    var dataSources: Map<String, HikariConfig> = emptyMap()
    var defaultLimit: Int = DEFAULT_LIMIT
    var maxLimit: Int = MAX_LIMIT
    var cacheExpirationMinutes: Long = DEFAULT_CACHE_EXPIRATION_MINUTES
    var itemCacheExpirationMinutes: Long = DEFAULT_ITEM_CACHE_EXPIRATION_MINUTES
    var trimStrings: Boolean = true

    companion object {
        private const val DEFAULT_LIMIT = 25
        private const val MAX_LIMIT = 5
        private const val DEFAULT_CACHE_EXPIRATION_MINUTES: Long = 10
        private const val DEFAULT_ITEM_CACHE_EXPIRATION_MINUTES: Long = 1440
    }
}