package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.data")
class DataProps {
    var defaultLimit: Int = DEFAULT_LIMIT
    var maxLimit: Int = MAX_LIMIT

    companion object {
        private const val DEFAULT_LIMIT = 25
        private const val MAX_LIMIT = 5
    }
}