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
    var itemCacheExpirationMinutes: Long = DEFAULT_ITEM_CACHE_EXPIRATION_MINUTES
    var permissionIdsCacheExpirationMinutes: Long = DEFAULT_PERMISSION_IDS_CACHE_EXPIRATION_MINUTES
    var revisionPolicyCacheExpirationMinutes: Long = DEFAULT_REVISION_POLICY_CACHE_EXPIRATION_MINUTES
    var sequenceCacheExpirationMinutes: Long = DEFAULT_SEQUENCE_CACHE_EXPIRATION_MINUTES
    var userCacheExpirationMinutes: Long = DEFAULT_USER_CACHE_EXPIRATION_MINUTES

    companion object {
        private const val DEFAULT_LIMIT = 25
        private const val MAX_LIMIT = 5
        private const val DEFAULT_ITEM_CACHE_EXPIRATION_MINUTES: Long = 1440
        private const val DEFAULT_PERMISSION_IDS_CACHE_EXPIRATION_MINUTES: Long = 10
        private const val DEFAULT_REVISION_POLICY_CACHE_EXPIRATION_MINUTES: Long = 10
        private const val DEFAULT_SEQUENCE_CACHE_EXPIRATION_MINUTES: Long = 10
        private const val DEFAULT_USER_CACHE_EXPIRATION_MINUTES: Long = 10
    }
}