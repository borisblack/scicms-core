package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.i18n")
class I18nProps {
    var defaultLocale: String = DEFAULT_LOCALE

    companion object {
        private const val DEFAULT_LOCALE = "en-US"
    }
}
