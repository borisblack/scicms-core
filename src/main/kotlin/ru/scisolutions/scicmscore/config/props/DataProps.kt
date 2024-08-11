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
    var datasourceCacheExpirationMinutes: Long = DEFAULT_DATASOURCE_CACHE_EXPIRATION_MINUTES
    var itemQueryResultEntryTtlMinutes: Int = DEFAULT_ITEM_QUERY_RESULT_ENTRY_TTL_MINUTES
    var itemQueryResultMaxEntries: Int = DEFAULT_ITEM_QUERY_RESULT_MAX_ENTRIES
    var datasetQueryResultEntryTtlMinutes: Int = DEFAULT_DATASET_QUERY_RESULT_ENTRY_TTL_MINUTES
    var datasetQueryResultMaxEntries: Int = DEFAULT_DATASET_QUERY_RESULT_MAX_ENTRIES
    var maxCachedRecordsSize: Int = DEFAULT_MAX_CACHED_RECORDS_SIZE
    var trimStrings: Boolean = true

    companion object {
        private const val DEFAULT_POOL_SIZE = 5
        private const val DEFAULT_IDLE = 1
        private const val DEFAULT_LIMIT = 20
        private const val MAX_LIMIT = 1000
        private const val DEFAULT_DATA_LOADER_CHUNK_SIZE = 1000
        private const val DEFAULT_DATASOURCE_CACHE_EXPIRATION_MINUTES: Long = 720
        private const val DEFAULT_ITEM_QUERY_RESULT_ENTRY_TTL_MINUTES: Int = 10
        private const val DEFAULT_ITEM_QUERY_RESULT_MAX_ENTRIES: Int = 50
        private const val DEFAULT_DATASET_QUERY_RESULT_ENTRY_TTL_MINUTES: Int = 5
        private const val DEFAULT_DATASET_QUERY_RESULT_MAX_ENTRIES: Int = 5
        private const val DEFAULT_MAX_CACHED_RECORDS_SIZE: Int = 200
    }
}
