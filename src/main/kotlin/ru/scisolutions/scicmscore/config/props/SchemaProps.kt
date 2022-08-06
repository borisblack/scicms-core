package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.schema")
class SchemaProps {
    var path: String? = null
    var readOnInit: Boolean = true
    var seedOnInit: Boolean = true
    var deleteIfAbsent: Boolean = false
    var itemsLockDurationSeconds: Long = DEFAULT_ITEMS_LOCK_DURATION_SECONDS

    companion object {
        private const val DEFAULT_ITEMS_LOCK_DURATION_SECONDS: Long = 300
    }
}