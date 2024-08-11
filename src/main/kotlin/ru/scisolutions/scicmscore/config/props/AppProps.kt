package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core")
class AppProps {
    var coreVersion: String = DEFAULT_CORE_VERSION

    companion object {
        private const val DEFAULT_CORE_VERSION = "v1"
    }
}
