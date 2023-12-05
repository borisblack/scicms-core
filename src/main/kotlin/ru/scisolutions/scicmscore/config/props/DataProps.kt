package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.data")
class DataProps {
    var defaultPoolSize: Int = DEFAULT_POOL_SIZE
    var defaultIdle: Int = DEFAULT_IDLE
    var defaultLimit: Int = DEFAULT_LIMIT
    var maxLimit: Int = MAX_LIMIT
    var dataLoaderChunkSize = DEFAULT_DATA_LOADER_CHUNK_SIZE
    var cacheExpirationMinutes: Long = DEFAULT_CACHE_EXPIRATION_MINUTES
    var trimStrings: Boolean = true

    companion object {
        private const val DEFAULT_POOL_SIZE = 5
        private const val DEFAULT_IDLE = 1
        private const val DEFAULT_LIMIT = 20
        private const val MAX_LIMIT = 1000
        private const val DEFAULT_DATA_LOADER_CHUNK_SIZE = 1000
        private const val DEFAULT_CACHE_EXPIRATION_MINUTES: Long = 720
    }
}