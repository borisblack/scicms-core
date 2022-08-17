package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.schema")
class SchemaProps {
    var path: String? = null
    var useFileChecksum: Boolean = true
    var seedOnInit: Boolean = true
    var deleteIfAbsent: Boolean = false
    var itemLockLockDurationSeconds: Long = DEFAULT_ITEM_LOCK_LOCK_DURATION_SECONDS
    var tryRecreateAttributes: Boolean = true

    companion object {
        private const val DEFAULT_ITEM_LOCK_LOCK_DURATION_SECONDS: Long = 300
    }
}