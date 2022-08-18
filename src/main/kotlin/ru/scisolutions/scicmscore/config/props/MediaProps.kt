package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.media")
class MediaProps {
    var provider: String = DEFAULT_PROVIDER
    var includeInUniqueIndex: Boolean = true

    @NestedConfigurationProperty
    var providerOptions: ProviderOptions = ProviderOptions()

    class ProviderOptions {
        var basePath: String? = null
        var createDirectories: Boolean = true
    }

    companion object {
        private const val DEFAULT_PROVIDER = "local"
    }
}