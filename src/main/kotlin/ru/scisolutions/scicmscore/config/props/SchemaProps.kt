package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.schema")
class SchemaProps {
    var path: String? = null

    /**
     * Flag to prevent restoring changed items (made via API) from scheme files (should be true in production)
     */
    var useFileChecksum: Boolean = true
    var seedOnInit: Boolean = true
    var deleteIfAbsent: Boolean = false

    /**
     * Rebuild unique attribute indexes on item's version/localized flag(s) change
     */
    var rebuildUniqueAttributeIndexes: Boolean = true
    var lockDurationSeconds: Long = DEFAULT_LOCK_DURATION_SECONDS

    companion object {
        private const val DEFAULT_LOCK_DURATION_SECONDS: Long = 300
    }
}