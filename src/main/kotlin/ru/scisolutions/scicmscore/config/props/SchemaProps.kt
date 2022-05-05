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
}